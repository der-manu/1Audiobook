package voice.bookOverview.internetCover

import com.squareup.anvil.annotations.ContributesMultibinding
import voice.bookOverview.bottomSheet.BottomSheetItem
import voice.bookOverview.bottomSheet.BottomSheetItemViewModel
import voice.bookOverview.di.BookOverviewScope
import voice.common.BookId
import voice.common.navigation.Destination
import voice.common.navigation.Navigator
import voice.common.pref.PrefKeys
import voice.pref.Pref
import javax.inject.Inject
import javax.inject.Named

@BookOverviewScope
@ContributesMultibinding(BookOverviewScope::class)
class InternetCoverViewModel
@Inject
constructor(
  private val navigator: Navigator,
  @Named(PrefKeys.USE_ONLINE_COVER_SEARCH)
  private val useOnlineCoverSearch: Pref<Boolean>,
) : BottomSheetItemViewModel {

  /** Die Suche sendet den Buchtitel an DuckDuckGo und gibt es deshalb nur, wenn der Nutzer sie eingeschaltet hat. */
  override suspend fun items(bookId: BookId): List<BottomSheetItem> {
    return if (useOnlineCoverSearch.value) listOf(BottomSheetItem.InternetCover) else emptyList()
  }

  override suspend fun onItemClick(
    bookId: BookId,
    item: BottomSheetItem,
  ) {
    if (item == BottomSheetItem.InternetCover) {
      navigator.goTo(Destination.CoverFromInternet(bookId))
    }
  }
}
