package voice.app.injection

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import voice.app.BuildConfig
import voice.app.serialization.UriSerializer
import voice.bookOverview.BookMigrationExplanationQualifier
import voice.bookOverview.BookMigrationExplanationShown
import voice.common.AppScope
import voice.common.BookId
import voice.common.constants.*
import voice.common.grid.GridMode
import voice.common.pref.AuthorAudiobookFolders
import voice.common.pref.CurrentBook
import voice.common.pref.OnboardingCompleted
import voice.common.pref.PrefKeys
import voice.common.pref.LibraryAudiobookFolders
import voice.common.pref.LibraryBookFolders
import voice.common.pref.RootAudiobookFolders
import voice.common.pref.SingleFileAudiobookFolders
import voice.common.pref.SingleFolderAudiobookFolders
import voice.datastore.VoiceDataStoreFactory
import voice.pref.AndroidPreferences
import voice.pref.Pref
import voice.pref.boolean
import voice.pref.enum
import voice.pref.int
import voice.pref.string
import voice.pref.stringSet
import java.time.LocalTime
import javax.inject.Named
import javax.inject.Singleton

@Module
@ContributesTo(AppScope::class)
object PrefsModule {

  @Provides
  @Singleton
  fun provideSharedPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences("${BuildConfig.APPLICATION_ID}_preferences", Context.MODE_PRIVATE)
  }

  @Provides
  @Singleton
  fun prefs(sharedPreferences: SharedPreferences): AndroidPreferences {
    return AndroidPreferences(sharedPreferences)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.AUTO_REWIND_AMOUNT)
  fun provideAutoRewindAmountPreference(prefs: AndroidPreferences): Pref<Int> {
    return prefs.int(PrefKeys.AUTO_REWIND_AMOUNT, 2)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.SEEK_TIME)
  fun provideSeekTimePreference(prefs: AndroidPreferences): Pref<Int> {
    return prefs.int(PrefKeys.SEEK_TIME, 30)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.SLEEP_TIME)
  fun provideSleepTimePreference(prefs: AndroidPreferences): Pref<Int> {
    return prefs.int(PrefKeys.SLEEP_TIME, 15)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.AUTO_SLEEP_TIMER)
  fun provideAutoSleepTimerPreference(prefs: AndroidPreferences): Pref<Boolean> {
    return prefs.boolean(PrefKeys.AUTO_SLEEP_TIMER, false)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.AUTO_SLEEP_TIMER_START)
  fun provideAutoSleepTimerStartPreference(prefs: AndroidPreferences): Pref<String> {
    return prefs.string(PrefKeys.AUTO_SLEEP_TIMER_START, LocalTime.of(22, 0).toString())
  }

  @Provides
  @Singleton
  @Named(PrefKeys.AUTO_SLEEP_TIMER_END)
  fun provideAutoSleepTimerEndPreference(prefs: AndroidPreferences): Pref<String> {
    return prefs.string(PrefKeys.AUTO_SLEEP_TIMER_END, LocalTime.of(5, 0).toString())
  }

  @Provides
  @Singleton
  @Named(PrefKeys.RESET_SLEEP_TIMER_ON_PLAYBACK_ACTION)
  fun provideResetSleepTimerOnPlaybackActionPreference(prefs: AndroidPreferences): Pref<Boolean> {
    return prefs.boolean(PrefKeys.RESET_SLEEP_TIMER_ON_PLAYBACK_ACTION, true)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.GRID_MODE)
  fun gridViewPref(prefs: AndroidPreferences): Pref<GridMode> {
    return prefs.enum(PrefKeys.GRID_MODE, GridMode.FOLLOW_DEVICE)
  }

  @Provides
  @Singleton
  @OnboardingCompleted
  fun onboardingCompleted(factory: VoiceDataStoreFactory): DataStore<Boolean> {
    return factory.boolean("onboardingCompleted", defaultValue = false)
  }

  @Provides
  @Singleton
  @RootAudiobookFolders
  fun audiobookFolders(factory: VoiceDataStoreFactory): DataStore<List<Uri>> {
    return factory.createUriList("audiobookFolders")
  }

  @Provides
  @Singleton
  @SingleFolderAudiobookFolders
  fun singleFolderAudiobookFolders(factory: VoiceDataStoreFactory): DataStore<List<Uri>> {
    return factory.createUriList("SingleFolderAudiobookFolders")
  }

  @Provides
  @Singleton
  @SingleFileAudiobookFolders
  fun singleFileAudiobookFolders(factory: VoiceDataStoreFactory): DataStore<List<Uri>> {
    return factory.createUriList("SingleFileAudiobookFolders")
  }

  @Provides
  @Singleton
  @AuthorAudiobookFolders
  fun authorAudiobookFolders(factory: VoiceDataStoreFactory): DataStore<List<Uri>> {
    return factory.createUriList("AuthorAudiobookFolders")
  }

  @Provides
  @Singleton
  @LibraryAudiobookFolders
  fun libraryAudiobookFolders(factory: VoiceDataStoreFactory): DataStore<List<Uri>> {
    return factory.createUriList("LibraryAudiobookFolders")
  }

  @Provides
  @Singleton
  @LibraryBookFolders
  fun libraryBookFolders(factory: VoiceDataStoreFactory): DataStore<List<Uri>> {
    return factory.createUriList("LibraryBookFolders")
  }

  @Provides
  @Singleton
  @CurrentBook
  fun currentBook(factory: VoiceDataStoreFactory): DataStore<BookId?> {
    return factory.create(
      serializer = BookId.serializer().nullable,
      fileName = "currentBook",
      defaultValue = null,
    )
  }

  @Provides
  @Singleton
  @BookMigrationExplanationQualifier
  fun bookMigrationExplanationShown(factory: VoiceDataStoreFactory): BookMigrationExplanationShown {
    return factory.create(Boolean.serializer(), false, "bookMigrationExplanationShown2")
  }

  //PlayBook
  @Provides
  @Singleton
  @Named(PrefKeys.THEME)
  fun themePref(prefs: AndroidPreferences): Pref<Int> {
    return prefs.int(PrefKeys.THEME, THEME_LIGHT)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.COLOR_THEME)
  fun colorThemePreference(prefs: AndroidPreferences): Pref<Int> {
    return prefs.int(PrefKeys.COLOR_THEME, Color(red = 76, green = 134, blue = 203).toArgb())
  }

  @Provides
  @Singleton
  @Named(PrefKeys.THEME_WIDGET)
  fun themeWidgetPref(prefs: AndroidPreferences): Pref<Int> {
    return prefs.int(PrefKeys.THEME_WIDGET, THEME_LIGHT)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.SEEK_TIME_REWIND)
  fun provideSeekTimeRewindPreference(prefs: AndroidPreferences): Pref<Int> {
    return prefs.int(PrefKeys.SEEK_TIME_REWIND, 20)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.SKIP_BUTTON_STYLE)
  fun skipButtonStylePref(prefs: AndroidPreferences): Pref<Int> {
    return prefs.int(PrefKeys.SKIP_BUTTON_STYLE, SKIP_BUTTON_ROUND)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.PLAY_BUTTON_STYLE)
  fun playButtonStylePref(prefs: AndroidPreferences): Pref<Int> {
    return prefs.int(PrefKeys.PLAY_BUTTON_STYLE, PLAY_BUTTON_ROUND_AND_SQUARE)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.MINI_PLAYER_STYLE)
  fun miniPlayerStylePref(prefs: AndroidPreferences): Pref<Int> {
    return prefs.int(PrefKeys.MINI_PLAYER_STYLE, MINI_PLAYER_PLAYER)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.CURRENT_VOLUME)
  fun currentVolumePreference(prefs: AndroidPreferences): Pref<Int> {
    return prefs.int(PrefKeys.CURRENT_VOLUME, 0)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.SHOW_SLIDER_VOLUME)
  fun showSliderVolumePref(prefs: AndroidPreferences): Pref<Boolean> {
    return prefs.boolean(PrefKeys.SHOW_SLIDER_VOLUME, true)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.TRANSPARENT_NAVIGATION)
  fun useTransparentNavigationPref(prefs: AndroidPreferences): Pref<Boolean> {
    return prefs.boolean(PrefKeys.TRANSPARENT_NAVIGATION, true)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.PADDING)
  fun paddingPref(prefs: AndroidPreferences): Pref<String> {
    return prefs.string(PrefKeys.PADDING, "0;0;0;0") //padding in Dp for edge to edge
  }

  @Provides
  @Singleton
  @Named(PrefKeys.PLAYER_BACKGROUND)
  fun playerBackgroundPref(prefs: AndroidPreferences): Pref<Int> {
    return prefs.int(PrefKeys.PLAYER_BACKGROUND, PLAYER_BACKGROUND_THEME)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.REPEAT_MODE)
  fun repeatModePreference(prefs: AndroidPreferences): Pref<Int> {
    return prefs.int(PrefKeys.REPEAT_MODE, REPEAT_OFF)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.SORTING_CURRENT)
  fun sortingCurrentPref(prefs: AndroidPreferences): Pref<Int> {
    return prefs.int(PrefKeys.SORTING_CURRENT, SORTING_LAST)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.SORTING_NOT_STARTED)
  fun sortingNotStartedPref(prefs: AndroidPreferences): Pref<Int> {
    return prefs.int(PrefKeys.SORTING_NOT_STARTED, SORTING_NAME)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.SORTING_FINISHED)
  fun sortingFinishedPref(prefs: AndroidPreferences): Pref<Int> {
    return prefs.int(PrefKeys.SORTING_FINISHED, SORTING_LAST)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.SORTING_BOOKMARK)
  fun sortingBookmarkPref(prefs: AndroidPreferences): Pref<Int> {
    return prefs.int(PrefKeys.SORTING_BOOKMARK, SORTING_BOOKMARK_LAST)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.USE_GESTURES)
  fun useGestures(prefs: AndroidPreferences): Pref<Boolean> {
    return prefs.boolean(PrefKeys.USE_GESTURES, true)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.USE_HAPTIC_FEEDBACK)
  fun useHapticFeedback(prefs: AndroidPreferences): Pref<Boolean> {
    return prefs.boolean(PrefKeys.USE_HAPTIC_FEEDBACK, false)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.USE_ONLINE_COVER_SEARCH)
  fun useOnlineCoverSearch(prefs: AndroidPreferences): Pref<Boolean> {
    // Standard: aus. Ohne ausdrueckliche Zustimmung nutzt die App das Internet nicht.
    return prefs.boolean(PrefKeys.USE_ONLINE_COVER_SEARCH, false)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.SCAN_COVER_CHAPTER)
  fun scanCoverChapter(prefs: AndroidPreferences): Pref<Boolean> {
    return prefs.boolean(PrefKeys.SCAN_COVER_CHAPTER, true)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.USE_MENU_ICONS)
  fun useMenuIconsPref(prefs: AndroidPreferences): Pref<Boolean> {
    return prefs.boolean(PrefKeys.USE_MENU_ICONS, false)
  }

  @Provides
  @Singleton
  @Named(PrefKeys.USE_ANIMATED_MARQUEE)
  fun useAnimatedMarqueePref(prefs: AndroidPreferences): Pref<Boolean> {
    return prefs.boolean(PrefKeys.USE_ANIMATED_MARQUEE, false)
  }
}

private fun VoiceDataStoreFactory.createUriList(name: String): DataStore<List<Uri>> = create(
  serializer = ListSerializer(UriSerializer),
  fileName = name,
  defaultValue = emptyList(),
)
