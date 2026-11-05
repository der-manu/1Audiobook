package voice.cover.api

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import voice.common.AppScope
import javax.inject.Singleton

@ContributesTo(AppScope::class)
@Module
object CoverModule {

  @Provides
  @Singleton
  fun client(): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .addInterceptor { chain ->
      val request = chain.request()
      val builder = request.newBuilder()
        .header("User-Agent", USER_AGENT)
        .header("Accept-Language", "en-US,en;q=0.9,de;q=0.8")
      // Die Suchseite erwartet, dass die Anfrage von ihr selbst kommt. Fremde Bildserver bekommen das nicht.
      if (request.url.host.endsWith("duckduckgo.com")) builder.header("Referer", "https://duckduckgo.com/")
      chain.proceed(builder.build())
    }
    .build()

  private const val USER_AGENT =
    "Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) " +
      "Chrome/124.0.0.0 Mobile Safari/537.36"

  @Provides
  @Singleton
  fun internalApi(client: OkHttpClient): InternalCoverApi {
    val json = Json {
      ignoreUnknownKeys = true
    }
    return Retrofit.Builder()
      .addConverterFactory(ScalarsConverterFactory.create())
      .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
      .baseUrl("https://duckduckgo.com/")
      .client(client)
      .build()
      .create()
  }
}
