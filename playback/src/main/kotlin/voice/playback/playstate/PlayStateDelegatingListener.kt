package voice.playback.playstate

import androidx.media3.common.Player
import voice.playback.AutoSleepTimerHandler
import javax.inject.Inject

class PlayStateDelegatingListener
@Inject constructor(
  private val playStateManager: PlayStateManager,
  private val autoSleepTimerHandler: AutoSleepTimerHandler
) : Player.Listener {

  private lateinit var player: Player

  fun attachTo(player: Player) {
    this.player = player
    player.addListener(this)
    updatePlayState()
  }

  override fun onPlaybackStateChanged(playbackState: Int) {
    updatePlayState()
  }

  override fun onPlayWhenReadyChanged(
    playWhenReady: Boolean,
    reason: Int,
  ) {
    updatePlayState()
  }

  private fun updatePlayState() {
    val playbackState = player.playbackState
    val newState = when {
      playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE -> PlayStateManager.PlayState.Paused
      player.playWhenReady -> PlayStateManager.PlayState.Playing
      else -> PlayStateManager.PlayState.Paused
    }

    // Check if we just transitioned to Playing
    if (newState == PlayStateManager.PlayState.Playing && playStateManager.playState != PlayStateManager.PlayState.Playing) {
      autoSleepTimerHandler.onPlaying()
    }

    playStateManager.playState = newState
  }
}
