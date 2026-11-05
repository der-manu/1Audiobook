package voice.cover.api

import voice.logging.core.Logger
import javax.inject.Inject

class CoverApi
@Inject constructor(private val api: InternalCoverApi) {

  internal suspend fun token(query: String): String? {
    Logger.d("query token")
    val response = api.auth(query)
    return parseVqd(response)
  }

  internal suspend fun search(
    query: String,
    auth: String,
    url: String = "/i.js",
  ): SearchResponse {
    Logger.d("search $query, url=$url")
    // Beim ersten Aufruf gehoeren die Standard-Parameter dazu; Folgeseiten bringen sie in ihrer Adresse schon mit.
    val firstPage = url == FIRST_PAGE
    return api.search(
      url = url,
      query = query,
      auth = auth,
      region = if (firstPage) "wt-wt" else null,
      output = if (firstPage) "json" else null,
      filters = if (firstPage) ",,,,," else null,
      safeSearch = if (firstPage) "1" else null,
    )
  }

  private companion object {
    const val FIRST_PAGE = "/i.js"
  }
}
