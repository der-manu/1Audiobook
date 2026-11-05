package voice.settings.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squareup.anvil.annotations.ContributesTo
import voice.common.AppScope
import voice.common.compose.VoiceTheme
import voice.common.compose.rememberScoped
import voice.common.rootComponentAs
import voice.strings.R as StringsR
import voice.common.R as CommonR

@Composable
@Preview
fun AboutPreview() {
  val viewState = AboutViewState(
    appVersion = "1.2.3",
    paddings = "0;0;0;0",
  )
  VoiceTheme(preview = true) {
    About(
      viewState,
      object : AboutListener {
        override fun close() {}
        override fun onUrlClick(url: String) {}
      },
    )
  }
}

@Composable
private fun About(viewState: AboutViewState, listener: AboutListener) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  val top = viewState.paddings.substringBefore(';').toInt()
  val bottom = viewState.paddings.substringAfter(';').substringBefore(';').toInt()
  val start = viewState.paddings.substringBeforeLast(';').substringAfterLast(';').toInt()
  val end = viewState.paddings.substringAfterLast(';').toInt()
  Scaffold(
    modifier = Modifier
      .nestedScroll(scrollBehavior.nestedScrollConnection)
      .padding(start = start.dp, end = end.dp),
    topBar = {
      TopAppBar(
        title = {
          Text(stringResource(CommonR.string.pref_about_title))
        },
        scrollBehavior = scrollBehavior,
        navigationIcon = {
          IconButton(
            onClick = {
              listener.close()
            },
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
              contentDescription = stringResource(StringsR.string.close),
            )
          }
        },
        windowInsets = WindowInsets(top = top.dp),
      )
    },
  ) { contentPadding ->
    Box(
      Modifier
        .padding(contentPadding)
        .verticalScroll(rememberScrollState()),
    ) {
      Column(Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp, bottom = 26.dp)) {
        ListItem(
          modifier = Modifier.fillMaxWidth(),
          leadingContent = {
            Icon(
              modifier = Modifier.size(72.dp),
              painter = painterResource(id = CommonR.drawable.ic_app_logo),
              contentDescription = stringResource(id = CommonR.string.app_name),
            )
          },
          headlineContent = {
            Text(
              modifier = Modifier.padding(start = 12.dp),
              text = stringResource(CommonR.string.app_name),
              fontSize = 18.sp,
            )
          },
          supportingContent = {
            Text(
              modifier = Modifier.padding(start = 12.dp),
              text = stringResource(StringsR.string.pref_app_version) + " " + viewState.appVersion,
              color = LocalContentColor.current.copy(alpha = 0.5F),
            )
          },
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
          modifier = Modifier.padding(start = 4.dp),
          text = stringResource(CommonR.string.about_description),
          style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.size(16.dp))
        LinkCard(
          icon = Icons.Rounded.Code,
          title = stringResource(CommonR.string.about_source_code),
          onClick = { listener.onUrlClick(Links.PROJECT) },
        )
        Spacer(modifier = Modifier.size(16.dp))
        LinkCard(
          icon = Icons.Rounded.BugReport,
          title = stringResource(CommonR.string.about_report_issue),
          onClick = { listener.onUrlClick(Links.ISSUES) },
        )
        Spacer(modifier = Modifier.size(16.dp))
        LinkCard(
          icon = Icons.Rounded.Policy,
          title = stringResource(CommonR.string.pref_privacy_title),
          onClick = { listener.onUrlClick(Links.PRIVACY) },
        )
        Spacer(modifier = Modifier.size(24.dp))

        Text(
          modifier = Modifier.padding(start = 4.dp),
          text = stringResource(CommonR.string.about_credits_title),
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 8.dp),
          text = stringResource(CommonR.string.about_credits_intro),
          style = MaterialTheme.typography.bodySmall,
          color = LocalContentColor.current.copy(alpha = 0.7F),
        )
        CreditSection(
          title = stringResource(CommonR.string.about_credits_based_on),
          credits = Credits.basedOn,
          onUrlClick = listener::onUrlClick,
        )
        CreditSection(
          title = stringResource(CommonR.string.about_credits_inspiration),
          credits = Credits.inspiration,
          onUrlClick = listener::onUrlClick,
        )
        CreditSection(
          title = stringResource(CommonR.string.about_credits_libraries),
          credits = Credits.libraries,
          onUrlClick = listener::onUrlClick,
        )
        Spacer(modifier = Modifier.size(bottom.dp))
      }
    }
  }
}

@Composable
private fun LinkCard(
  icon: ImageVector,
  title: String,
  onClick: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseOnSurface),
    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .clickable { onClick() },
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        modifier = Modifier
          .padding(start = 16.dp, end = 8.dp)
          .weight(1f),
        text = title.toUpperCase(LocaleList.current),
        fontSize = 16.sp,
        lineHeight = 18.sp,
      )
      Box(modifier = Modifier.padding(end = 8.dp, top = 8.dp, bottom = 8.dp).width(42.dp)) {
        Icon(
          modifier = Modifier.alpha(0.2f).size(42.dp),
          imageVector = Icons.Rounded.Circle,
          contentDescription = null,
        )
        Icon(
          modifier = Modifier.size(42.dp).padding(8.dp),
          imageVector = icon,
          contentDescription = title,
        )
      }
    }
  }
}

@Composable
private fun CreditSection(
  title: String,
  credits: List<Credit>,
  onUrlClick: (String) -> Unit,
) {
  Text(
    modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 2.dp),
    text = title.toUpperCase(LocaleList.current),
    style = MaterialTheme.typography.labelMedium,
    color = LocalContentColor.current.copy(alpha = 0.6F),
  )
  credits.forEach { credit ->
    val url = credit.url
    ListItem(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(enabled = url != null) { if (url != null) onUrlClick(url) },
      headlineContent = { Text(text = credit.name) },
      supportingContent = { Text(text = credit.detail) },
    )
  }
}

@ContributesTo(AppScope::class)
interface AboutComponent {
  val aboutViewModel: AboutViewModel
}

@Composable
fun About() {
  val viewModel = rememberScoped { rootComponentAs<AboutComponent>().aboutViewModel }
  val viewState = viewModel.viewState()
  About(viewState, viewModel)
}
