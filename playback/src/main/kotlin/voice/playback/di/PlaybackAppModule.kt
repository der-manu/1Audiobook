package voice.playback.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import voice.common.AppScope
import voice.playback.PlayerController
import voice.playback.playstate.PlayStateManager
import voice.sleepTimer.SleepTimerPlayStateProvider
import voice.sleepTimer.SleepTimerPlayerControl

@Suppress("unused")
@Module
@ContributesTo(AppScope::class)
abstract class PlaybackAppModule {

  @Binds
  abstract fun bindSleepTimerPlayStateProvider(
    playStateManager: PlayStateManager
  ): SleepTimerPlayStateProvider

  @Binds
  abstract fun bindSleepTimerPlayerControl(
    playerController: PlayerController
  ): SleepTimerPlayerControl
}
