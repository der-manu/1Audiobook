package voice.cover.api

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
  val next: String? = null,
  val results: List<ImageResult>,
) {

  @Serializable
  data class ImageResult(
    val width: Int = 0,
    val height: Int = 0,
    val image: String,
    val thumbnail: String,
  )
}
