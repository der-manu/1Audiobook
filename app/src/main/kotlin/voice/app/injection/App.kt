package voice.app.injection

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatDelegate
import coil.Coil
import coil.ImageLoader
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import voice.app.features.widget.TriggerWidgetOnChange
import voice.app.scanner.MediaScanTrigger
import voice.common.DARK_THEME_SETTABLE
import voice.common.constants.THEME_DARK
import voice.common.constants.THEME_LIGHT
import voice.common.pref.PrefKeys
import voice.common.rootComponent
import voice.common.R as CommonR
import voice.pref.Pref
import javax.inject.Inject
import javax.inject.Named

class App : Application() {

  @Inject
  lateinit var mediaScanner: MediaScanTrigger

  @Inject
  lateinit var triggerWidgetOnChange: TriggerWidgetOnChange

  @field:[Inject Named(PrefKeys.THEME)]
  lateinit var themePref: Pref<Int>

  override fun onCreate() {
    super.onCreate()

    Coil.setImageLoader(
      ImageLoader.Builder(this)
        .addLastModifiedToFileCacheKey(false)
        .build(),
    )

    DynamicColors.applyToActivitiesIfAvailable(this)

    appComponent = AppComponent.factory()
      .create(this)
    rootComponent = appComponent
    appComponent.inject(this)

    if (DARK_THEME_SETTABLE) {
      MainScope().launch {
        themePref.flow
          .distinctUntilChanged()
          .collect { themePref ->
            val nightMode = if (themePref == THEME_DARK) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(nightMode)
          }
      }
    }

    mediaScanner.scan()

    triggerWidgetOnChange.init()
  }
}

lateinit var appComponent: AppComponent
  @VisibleForTesting set
