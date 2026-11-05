package voice.common.pref

import javax.inject.Qualifier

object PrefKeys {

  const val AUTO_REWIND_AMOUNT = "AUTO_REWIND"
  const val SEEK_TIME = "SEEK_TIME"
  const val SLEEP_TIME = "SLEEP_TIME"
  const val AUTO_SLEEP_TIMER = "AUTO_SLEEP_TIMER"
  const val AUTO_SLEEP_TIMER_START = "AUTO_SLEEP_TIMER_START"
  const val AUTO_SLEEP_TIMER_END = "AUTO_SLEEP_TIMER_END"
  const val RESET_SLEEP_TIMER_ON_PLAYBACK_ACTION = "RESET_SLEEP_TIMER_ON_PLAYBACK_ACTION"
  const val GRID_MODE = "gridView"

  //PlayBooks
  const val THEME = "THEME"
  const val COLOR_THEME = "COLOR_THEME"
  const val THEME_WIDGET = "THEME_WIDGET"
  const val SEEK_TIME_REWIND = "SEEK_TIME_REWIND"
  const val CURRENT_VOLUME = "CURRENT_VOLUME"
  const val SHOW_SLIDER_VOLUME = "SHOW_SLIDER_VOLUME"
  const val SKIP_BUTTON_STYLE = "SKIP_BUTTON_STYLE"
  const val PLAY_BUTTON_STYLE = "PLAY_BUTTON_STYLE"
  const val MINI_PLAYER_STYLE = "MINI_PLAYER_STYLE"
  const val PADDING = "PADDING"
  const val TRANSPARENT_NAVIGATION = "TRANSPARENT_NAVIGATION"
  const val PLAYER_BACKGROUND = "PLAYER_BACKGROUND"
  const val REPEAT_MODE = "REPEAT_MODE"
  const val SORTING_CURRENT = "SORTING_CURRENT"
  const val SORTING_NOT_STARTED = "SORTING_NOT_STARTED"
  const val SORTING_FINISHED = "SORTING_FINISHED"
  const val SORTING_BOOKMARK = "SORTING_BOOKMARK"
  const val USE_GESTURES = "USE_GESTURES"
  const val USE_HAPTIC_FEEDBACK = "USE_HAPTIC_FEEDBACK"
  const val SCAN_COVER_CHAPTER = "SCAN_COVER_CHAPTER"
  const val USE_ONLINE_COVER_SEARCH = "USE_ONLINE_COVER_SEARCH"
  const val USE_MENU_ICONS = "USE_MENU_ICONS"
  const val USE_ANIMATED_MARQUEE = "USE_ANIMATED_MARQUEE"

  //old
}

@Qualifier
annotation class OnboardingCompleted

@Qualifier
annotation class RootAudiobookFolders

@Qualifier
annotation class SingleFolderAudiobookFolders

@Qualifier
annotation class SingleFileAudiobookFolders

@Qualifier
annotation class AuthorAudiobookFolders

@Qualifier
annotation class LibraryAudiobookFolders

@Qualifier
annotation class LibraryBookFolders

@Qualifier
annotation class CurrentBook
