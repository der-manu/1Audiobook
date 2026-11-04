package voice.app.features

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.bluelinelabs.conductor.ChangeHandlerFrameLayout
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import voice.app.AppController
import voice.app.R
import voice.app.features.bookOverview.EditCoverDialogController
import voice.app.injection.appComponent
import voice.app.misc.conductor.asVerticalChangeHandlerTransaction
import voice.app.uitools.SettingsContentObserver
import voice.app.uitools.setWindowTransparency
import voice.bookmark.BookmarkController
import voice.common.AppInfoProvider
import voice.common.BookId
import voice.common.convertPixelsToDp
import voice.common.navigation.Destination
import voice.common.navigation.NavigationCommand
import voice.common.navigation.Navigator
import voice.common.pref.CurrentBook
import voice.common.pref.PrefKeys
import voice.playback.PlayerController
import voice.playback.session.search.BookSearchHandler
import voice.playback.session.search.BookSearchParser
import voice.playbackScreen.BookPlayController
import voice.pref.Pref
import java.util.ArrayList
import javax.inject.Inject
import javax.inject.Named
import voice.common.R as CommonR

class MainActivity : AppCompatActivity() {

  @field:[
    Inject
    CurrentBook
  ]
  lateinit var currentBook: DataStore<BookId?>

  @field:[Inject Named(PrefKeys.CURRENT_VOLUME)]
  lateinit var currentVolumePref: Pref<Int>

  @field:[Inject Named(PrefKeys.TRANSPARENT_NAVIGATION)]
  lateinit var useTransparentNavigationPref: Pref<Boolean>

  @field:[Inject Named(PrefKeys.PADDING)]
  lateinit var paddingPref: Pref<String>

  @field:[Inject Named(PrefKeys.PRO)]
  lateinit var isProPref: Pref<Boolean>

  @Inject
  lateinit var bookSearchParser: BookSearchParser

  @Inject
  lateinit var bookSearchHandler: BookSearchHandler

  @Inject
  lateinit var playerController: PlayerController

  @Inject
  lateinit var navigator: Navigator

  @Inject
  lateinit var appInfoProvider: AppInfoProvider

  private lateinit var mSettingsContentObserver: SettingsContentObserver

  private lateinit var router: Router

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent.inject(this)
    setTheme(R.style.AppTheme)
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()

    val root = ChangeHandlerFrameLayout(this)
    setContentView(root)

    // Edge to edge
    if (useTransparentNavigationPref.value) {
      setWindowTransparency(false) { statusBarSize, bottomNavigationBarSize, leftNavigationBarSize, rightNavigationBarSize ->
        Log.i(
          "System Bar Size",
          "statusBarSize=$statusBarSize bottomNavigationBarSize=$bottomNavigationBarSize " +
            "leftNavigationBarSize=$leftNavigationBarSize rightNavigationBarSize=$rightNavigationBarSize",
        )
        paddingPref.value = "${convertPixelsToDp(statusBarSize)};${convertPixelsToDp(bottomNavigationBarSize)};" +
          "${convertPixelsToDp(leftNavigationBarSize)};${convertPixelsToDp(rightNavigationBarSize)}"
      }
    } else {
      //val color = SurfaceColors.SURFACE_0.getColor(this)
      //window.navigationBarColor = color
      paddingPref.value = "0;0;0;0"
    }

    mSettingsContentObserver = SettingsContentObserver(this, Handler(Looper.getMainLooper()))
    applicationContext.contentResolver.registerContentObserver(Settings.System.CONTENT_URI, true, mSettingsContentObserver)

    router = Conductor.attachRouter(this, root, savedInstanceState)
      .setOnBackPressedDispatcherEnabled(true)
      .setPopRootControllerMode(Router.PopRootControllerMode.NEVER)
    if (!router.hasRootController()) {
      setupRouter()
    }

    lifecycleScope.launch {
      navigator.navigationCommands.collect { command ->
        when (command) {
          NavigationCommand.GoBack -> {
            if (router.backstack.lastOrNull()?.controller is AppController) {
              // AppController handles it's own navigation commands
            } else {
              router.popCurrentController()
            }
          }
          is NavigationCommand.GoTo -> {
            when (val destination = command.destination) {
              is Destination.Compose -> {
                // no-op
              }
              is Destination.Bookmarks -> {
                router.pushController(BookmarkController(destination.bookId).asVerticalChangeHandlerTransaction())
              }
              is Destination.Playback -> {
                lifecycleScope.launch {
                  currentBook.updateData { destination.bookId }
                  router.pushController(BookPlayController(destination.bookId).asVerticalChangeHandlerTransaction())
                }
              }
              is Destination.Website -> {
                try {
                  startActivity(Intent(Intent.ACTION_VIEW, destination.url.toUri()))
                } catch (exception: ActivityNotFoundException) {
                }
              }
              is Destination.EditCover -> {
                val args = EditCoverDialogController.Arguments(destination.cover, destination.bookId)
                EditCoverDialogController(args).showDialog(router)
              }
              is Destination.Activity -> {
                startActivity(destination.intent)
              }
            }
          }
          is NavigationCommand.Execute -> {
            // handled in AppController
          }
        }
      }
    }

    setupFromIntent(intent)
  }

  @SuppressLint("UnsafeIntentLaunch")
  override fun recreate() {
    if (Build.VERSION.SDK_INT <= 30) {
      super.recreate()
    } else {
      finish()
      startActivity(intent)
    }
  }

  override fun onResume() {
    super.onResume()
    currentVolumePref.value = mSettingsContentObserver.previousVolume

    // PlayBookAdvance: Alle Funktionen sind frei (bool is_pro_app = true).
    if (resources.getBoolean(CommonR.bool.is_pro_app)) isProPref.value = true
  }

  override fun onDestroy() {
    super.onDestroy()
    applicationContext.contentResolver.unregisterContentObserver(mSettingsContentObserver)
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setupFromIntent(intent)
  }

  private fun setupFromIntent(intent: Intent?) {
    bookSearchParser.parse(intent)?.let {
      runBlocking {
        bookSearchHandler.handle(it)
      }
    }
  }

  private fun setupRouter() {
    // if we should enter a book set the backstack and return early
    val goToBook = intent.getBooleanExtra(NI_GO_TO_BOOK, false)
    if (goToBook) {
      val bookId = runBlocking { currentBook.data.first() }
      if (bookId != null) {
        val bookShelf = RouterTransaction.with(AppController())
        val bookPlay = BookPlayController(bookId).asVerticalChangeHandlerTransaction()
        router.setBackstack(listOf(bookShelf, bookPlay), null)
        return
      }
    }

    // if we should play the current book, set the backstack and return early
    if (intent?.action == "playCurrent") {
      runBlocking { currentBook.data.first() }?.let { bookId ->
        val bookShelf = RouterTransaction.with(AppController())
        val bookPlay = BookPlayController(bookId).asVerticalChangeHandlerTransaction()
        router.setBackstack(listOf(bookShelf, bookPlay), null)
        playerController.play()
        return
      }
    }

    val rootTransaction = RouterTransaction.with(AppController())
    router.setRoot(rootTransaction)
  }

  companion object {

    private const val NI_GO_TO_BOOK = "niGotoBook"

    fun goToBookIntent(context: Context) = Intent(context, MainActivity::class.java).apply {
      putExtra(NI_GO_TO_BOOK, true)
      flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
    }
  }
}
