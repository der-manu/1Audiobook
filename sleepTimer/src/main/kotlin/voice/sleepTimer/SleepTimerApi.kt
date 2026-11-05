package voice.sleepTimer

import kotlinx.coroutines.flow.StateFlow
import voice.common.BookId
import kotlin.time.Duration

interface SleepTimerApi {
    val leftSleepTimeFlow: StateFlow<Duration>
    val sleepAtEocFlow: StateFlow<Boolean>

    /** The length of the volume fade-out window at the end of the timer. */
    val fadeOutDuration: Duration
    fun sleepTimerActive(): Boolean
    fun setActive(enable: Boolean)
    fun setEoc(enable: Boolean, bookId: BookId)

    /** True while the timer is in its final fade-out window (volume ramping down towards 0). */
    fun isFadingOut(): Boolean

    /**
     * Call whenever the user plays, pauses, or seeks. Resets a running timer back to its full
     * duration, unless the user has disabled this behavior via preference.
     */
    fun onPlaybackAction()

    /**
     * Call when a pause was intercepted because the timer was fading out. Always resets the
     * timer back to its full duration, regardless of the [onPlaybackAction] preference, since
     * that's the only way to bring the volume back up.
     */
    fun onFadeOutInterrupted()
}
