package voice.search

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import voice.common.BookId
import voice.data.Book
import voice.data.repo.BookContentRepo
import voice.data.repo.BookRepository
import voice.data.repo.ChapterRepo
import voice.data.repo.internals.AppDb
import java.util.UUID

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class BookSearchTest {

  @Suppress("SpellCheckingInspection")
  private val leuchtturm = book(
    name = "Die Ermordung des Leuchtturmwaerters",
    author = "Haruka Musterfrau",
  )

  private val lighthouse = book(
    name = "All Along the Lighthouse",
    author = "Max Mustermann",
  )

  private val unicorns = book(
    name = "All Along the Unicorns",
    author = "Max Mustermann",
  )

  @Test
  fun `search complete name`() = test {
    expectSearchResult("Die Ermordung des Leuchtturmwaerters", leuchtturm)
  }

  @Test
  fun `search single word in name`() = test {
    expectSearchResult("Ermordung", leuchtturm)
  }

  @Test
  fun `search partial word in name from beginning with whitespace`() = test {
    expectSearchResult("   Ermord  ", leuchtturm)
  }

  @Test
  fun `search single word in author`() = test {
    expectSearchResult("Musterfrau", leuchtturm)
  }

  @Test
  fun `search partial word in name from beginning`() = test {
    expectSearchResult("Ermord", leuchtturm)
  }

  @Test
  fun `multiple matches on author`() = test {
    expectSearchResult("Max", lighthouse, unicorns)
  }

  @Test
  fun `multiple matches on title`() = test {
    expectSearchResult("along", lighthouse, unicorns)
  }

  @Test
  fun `sql reserved chars do not crash`() = test {
    search("-Ermordung")
    search("\"")
    search("**")
    search("--")
    search("-*\"--*\"\"")
  }

  private fun test(run: suspend TestBase.() -> Unit) {
    val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDb::class.java)
      .build()
    val repo = BookRepository(
      chapterRepo = ChapterRepo(db.chapterDao()),
      contentRepo = BookContentRepo(db.bookContentDao()),
    )
    val search = BookSearch(
      dao = db.bookContentDao(),
      repo = repo,
    )

    val testBase = TestBase(search)

    suspend fun addBook(book: Book) {
      db.bookContentDao().insert(book.content)
      book.chapters.forEach {
        db.chapterDao().insert(it)
      }
    }

    runTest {
      addBook(leuchtturm)
      addBook(lighthouse)
      addBook(unicorns)

      // this ensures that inactive books are never accounted for
      addBook(leuchtturm.withNewIdAndInactive())
      addBook(lighthouse.withNewIdAndInactive())
      addBook(unicorns.withNewIdAndInactive())

      testBase.run()
      db.close()
    }
  }
}

private fun Book.withNewIdAndInactive(): Book {
  return update {
    it.copy(
      id = BookId(UUID.randomUUID().toString()),
      isActive = false,
    )
  }
}

private class TestBase(private val search: BookSearch) {

  suspend fun expectSearchResult(
    query: String,
    vararg expected: Book,
  ) {
    val result = search.search(query)
    result shouldContainExactly expected.toList()
  }

  suspend fun search(query: String) {
    search.search(query)
  }
}
