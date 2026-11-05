package voice.sleepTimer

import kotlin.time.Duration

interface SleepTimerPlayerControl {
    fun setVolume(volume: Float)
    fun pauseWithRewind(rewind: Duration)
    fun playPause()
    fun play()
}
