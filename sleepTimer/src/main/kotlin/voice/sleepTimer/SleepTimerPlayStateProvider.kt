package voice.sleepTimer

import kotlinx.coroutines.flow.Flow

interface SleepTimerPlayStateProvider {
    /** Emits the current playback state, true if playing, false otherwise. */
    val isPlayingFlow: Flow<Boolean>

    /** Gets the current playback state, true if playing, false otherwise. */
    fun isCurrentlyPlaying(): Boolean
}
