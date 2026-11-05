package voice.playback

import dagger.Lazy
import voice.common.pref.PrefKeys
import voice.logging.core.Logger
import voice.pref.Pref
import voice.sleepTimer.SleepTimerApi
import java.time.LocalTime
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Named

class AutoSleepTimerHandler @Inject constructor(
    private val sleepTimer: Lazy<SleepTimerApi>,
    @Named(PrefKeys.AUTO_SLEEP_TIMER)
    private val autoSleepTimerPref: Pref<Boolean>,
    @Named(PrefKeys.AUTO_SLEEP_TIMER_START)
    private val autoSleepTimerStartPref: Pref<String>,
    @Named(PrefKeys.AUTO_SLEEP_TIMER_END)
    private val autoSleepTimerEndPref: Pref<String>
) {

    fun onPlaying() {
        activateAutoSleepTimerIfNeeded()
    }

    private fun isCurrentTimeInRange(startTime: LocalTime, endTime: LocalTime): Boolean {
        val currentTime = LocalTime.now()
        return if (startTime <= endTime) {
            currentTime.isAfter(startTime) && currentTime.isBefore(endTime)
        } else {
            currentTime.isAfter(startTime) || currentTime.isBefore(endTime)
        }
    }

    private fun activateAutoSleepTimerIfNeeded() {
        if (autoSleepTimerPref.value && !sleepTimer.get().sleepTimerActive()) {
            try {
                val startTimeString = autoSleepTimerStartPref.value
                val endTimeString = autoSleepTimerEndPref.value
                if (startTimeString.isNotBlank() && endTimeString.isNotBlank()) {
                    val startTime = LocalTime.parse(startTimeString)
                    val endTime = LocalTime.parse(endTimeString)
                    if (isCurrentTimeInRange(startTime, endTime)) {
                        Logger.i("AutoSleepTimerHandler: Auto starting sleep timer.")
                        sleepTimer.get().setActive(true)
                    }
                } else {
                    Logger.w("AutoSleepTimerHandler: Auto sleep timer start/end time preference is blank.")
                }
            } catch (e: DateTimeParseException) {
                Logger.e(e, "AutoSleepTimerHandler: Error parsing auto sleep timer start/end time.")
            }
        }
    }
}
