package voice.settings.about

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import voice.common.AppInfoProvider
import voice.common.navigation.Destination
import voice.common.navigation.Navigator
import voice.common.pref.PrefKeys
import voice.pref.Pref
import javax.inject.Inject
import javax.inject.Named

class AboutViewModel
@Inject constructor(
  private val navigator: Navigator,
  private val appInfoProvider: AppInfoProvider,
  @Named(PrefKeys.PADDING)
  private val paddingPref: Pref<String>,
) : AboutListener {

  @Composable
  fun viewState(): AboutViewState {
    val paddings by remember { paddingPref.flow }.collectAsState(initial = "0;0;0;0")
    return AboutViewState(
      appVersion = appInfoProvider.versionName,
      paddings = paddings,
    )
  }

  override fun close() {
    navigator.goBack()
  }

  override fun onUrlClick(url: String) {
    navigator.goTo(Destination.Website(url))
  }
}
