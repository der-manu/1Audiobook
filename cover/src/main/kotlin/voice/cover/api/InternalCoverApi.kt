package voice.cover.api

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface InternalCoverApi {

  @GET("/")
  suspend fun auth(@Query("q") search: String): String

  @GET
  suspend fun search(
    @Url
    url: String,
    @Query("q")
    query: String,
    @Query("vqd")
    auth: String,
    @Query("l")
    region: String?,
    @Query("o")
    output: String?,
    @Query("f")
    filters: String?,
    @Query("p")
    safeSearch: String?,
  ): SearchResponse
}
