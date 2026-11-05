package voice.playback.playstate

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import voice.sleepTimer.SleepTimerPlayStateProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayStateManager
@Inject
constructor() : SleepTimerPlayStateProvider {

  private val _playState = MutableStateFlow(PlayState.Paused)

  val flow: StateFlow<PlayState>
    get() = _playState

  var playState: PlayState
    set(value) {
      _playState.value = value
    }
    get() = _playState.value

  // Implementation for SleepTimerPlayStateProvider
  override val isPlayingFlow: kotlinx.coroutines.flow.Flow<Boolean>
    get() = flow.map { it == PlayState.Playing }

  override fun isCurrentlyPlaying(): Boolean {
    return playState == PlayState.Playing
  }

  enum class PlayState {
    Playing,
    Paused,
  }
}
