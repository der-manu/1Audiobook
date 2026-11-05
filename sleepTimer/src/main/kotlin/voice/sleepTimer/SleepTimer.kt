package voice.sleepTimer

import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import voice.common.AppScope
import voice.common.BookId
import voice.common.pref.PrefKeys
import voice.data.repo.BookRepository
import voice.logging.core.Logger
import voice.pref.Pref
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Singleton
@ContributesBinding(AppScope::class, SleepTimerApi::class)
class SleepTimer
@Inject constructor(
  private val playStateProvider: SleepTimerPlayStateProvider,
  private val shakeDetector: ShakeDetector,
  @Named(PrefKeys.SLEEP_TIME)
  private val sleepTimePref: Pref<Int>,
  private val sleepTimerPlayerControl: SleepTimerPlayerControl,
  private val bookRepo: BookRepository,
  @Named(PrefKeys.RESET_SLEEP_TIMER_ON_PLAYBACK_ACTION)
  private val resetOnPlaybackActionPref: Pref<Boolean>,
) : SleepTimerApi {

  private val scope = MainScope()
  override val fadeOutDuration = 10.seconds
  private val shakeToResetTime = 1.minutes
  private var lastSetSleepTime: Duration = Duration.ZERO

  private val _leftSleepTime = MutableStateFlow(Duration.ZERO)
  private val _sleepAtEoc = MutableStateFlow(false)
  private var leftSleepTime: Duration
    get() = _leftSleepTime.value
    set(value) {
      _leftSleepTime.value = value
    }
  private var sleepAtEoc: Boolean
    get() = _sleepAtEoc.value
    set(value) {
      _sleepAtEoc.value = value
    }
  override val leftSleepTimeFlow: StateFlow<Duration> get() = _leftSleepTime
  override val sleepAtEocFlow: StateFlow<Boolean> get() = _sleepAtEoc

  override fun sleepTimerActive(): Boolean = sleepJob?.isActive == true && leftSleepTime > Duration.ZERO

  override fun isFadingOut(): Boolean = sleepTimerActive() && leftSleepTime < fadeOutDuration

  override fun onPlaybackAction() {
    if (resetOnPlaybackActionPref.value) {
      restartIfActive()
    }
  }

  override fun onFadeOutInterrupted() {
    restartIfActive()
  }

  private fun restartIfActive() {
    if (sleepTimerActive()) {
      Logger.i("Playback action detected while sleep timer is active. Resetting to $lastSetSleepTime.")
      setActive(lastSetSleepTime)
    }
  }

  private var sleepJob: Job? = null
  private var shakeDetectionJob: Job? = null

  override fun setActive(enable: Boolean) {
    Logger.i("enable=$enable")
    if (enable) {
      setActive()
    } else {
      cancel()
    }
  }

  override fun setEoc(enable: Boolean, bookId: BookId) {
    Logger.i("sleep at EOC enable=$enable")
    if (enable) {
      startEoc(bookId)
    } else {
      cancel()
    }
  }

  fun setActive(sleepTime: Duration = sleepTimePref.value.minutes) {
    Logger.i("Starting sleepTimer. Pause in $sleepTime.")
    lastSetSleepTime = sleepTime
    leftSleepTime = sleepTime
    sleepTimerPlayerControl.setVolume(1F)
    sleepJob?.cancel()
    shakeDetectionJob?.cancel()
    sleepJob = scope.launch {
      startSleepTimerCountdown()
      Logger.d("Wait for $shakeToResetTime for a shake")
      withTimeout(shakeToResetTime) {
        shakeDetector.detect()
        Logger.i("Shake detected. Reset sleep time")
        sleepTimerPlayerControl.play()
        setActive()
      }
      Logger.i("exiting")
    }
  }

  private fun startEoc(bookId: BookId) {
    Logger.i("Starting sleepTimer. Pause at end of chapter.")
    sleepAtEoc = true
    sleepJob?.cancel()
    shakeDetectionJob?.cancel()
    sleepJob = scope.launch {
      startSleepEocCountdown(bookId)
      sleepAtEoc = false
      Logger.d("Wait for $shakeToResetTime for a shake")
      withTimeout(shakeToResetTime) {
        shakeDetector.detect()
        Logger.i("Shake detected. Reset sleep time")
        sleepTimerPlayerControl.play()
        startEoc(bookId)
      }
      Logger.i("exiting")
    }
    sleepTimerPlayerControl.setVolume(1F)
  }

  private suspend fun startSleepTimerCountdown() {
    var interval = 500.milliseconds
    shakeDetectionJob = null

    while (leftSleepTime > Duration.ZERO) {
      suspendUntilPlaying()
      if (leftSleepTime < fadeOutDuration && shakeDetectionJob == null) {
        // Start shake detection when fade-out begins
        shakeDetectionJob = scope.launch {
          withTimeout(fadeOutDuration) {
            shakeDetector.detect()
            Logger.i("Shake detected during fade-out. Resetting timer.")
            sleepJob?.cancel() // Cancel the main sleep job
            sleepTimerPlayerControl.setVolume(1F)
            setActive() // Restart the timer
          }
        }
        interval = 200.milliseconds
      }

      if (leftSleepTime < fadeOutDuration) {
        updateVolumeForSleepTime()
      }

      delay(interval)
      leftSleepTime = (leftSleepTime - interval).coerceAtLeast(Duration.ZERO)
    }
    shakeDetectionJob?.cancel() // Cancel shake detection if timer finishes
    sleepTimerPlayerControl.pauseWithRewind(fadeOutDuration)
    sleepTimerPlayerControl.setVolume(1F)
  }

  private suspend fun startSleepEocCountdown(bookId: BookId) {
    var book = bookRepo.get(bookId) ?: return
    val chapter = book.content.currentChapterIndex

    while (true) {
      suspendUntilPlaying()

      book = bookRepo.get(bookId) ?: return
      if (chapter != book.content.currentChapterIndex) {
        break
      }

      val timeLeft = ((book.currentChapter.duration - book.content.positionInChapter) * book.content.playbackSpeed).toLong() / 2

      delay(timeLeft.coerceAtLeast(125).coerceAtMost(5000).milliseconds)
    }
    sleepTimerPlayerControl.playPause()
  }

  private fun updateVolumeForSleepTime() {
    val percentageOfTimeLeft = if (leftSleepTime == Duration.ZERO) {
      0F
    } else {
      (leftSleepTime / fadeOutDuration).toFloat()
    }.coerceIn(0F, 1F)

    val volume = 1 - FastOutSlowInInterpolator().getInterpolation(1 - percentageOfTimeLeft)
    sleepTimerPlayerControl.setVolume(volume)
  }

  private suspend fun suspendUntilPlaying() {
    if (!playStateProvider.isCurrentlyPlaying()) {
      Logger.i("Not playing. Wait for Playback to continue.")
      playStateProvider.isPlayingFlow
        .filter { it /* it is true when playing */ }
        .first()
      Logger.i("Playback continued.")
    }
  }

  private fun cancel() {
    sleepJob?.cancel()
    shakeDetectionJob?.cancel()
    leftSleepTime = Duration.ZERO
    sleepTimerPlayerControl.setVolume(1F)
    sleepAtEoc = false
  }
}
