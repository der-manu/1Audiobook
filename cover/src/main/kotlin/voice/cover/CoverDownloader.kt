package voice.cover

import android.content.Context
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.sink
import voice.logging.core.Logger
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CoverDownloader
@Inject constructor(
  private val client: OkHttpClient,
  private val context: Context,
) {

  internal suspend fun download(url: String): File? {
    val tempFolder = File(context.cacheDir, "coverDownload")
      .apply {
        deleteRecursively()
        mkdirs()
      }
    val request = Request.Builder()
      .url(url)
      .build()
    val response = try {
      client.newCall(request).await()
    } catch (e: IOException) {
      Logger.w(e, "Failed to download cover from $url")
      return null
    }
    // Viele Webseiten sperren das Laden ihrer Originalbilder und antworten mit einer Fehlerseite statt mit einem Bild.
    if (!response.isSuccessful) {
      Logger.w("Cover download from $url failed with HTTP ${response.code}")
      response.close()
      return null
    }
    return withContext(Dispatchers.IO) {
      try {
        val saved = response.body?.source()?.use { source ->
          // select a random name so on updating this, the old image is not cached
          val file = File(tempFolder, UUID.randomUUID().toString())
          file.sink().use { sink ->
            source.readAll(sink)
          }
          file
        }
        // Nur echte Bilder zaehlen. So greift der Aufrufer auf das Vorschaubild zurueck, statt eine HTML-Seite
        // als Cover zu uebernehmen.
        if (saved != null && saved.isDecodableImage()) {
          saved
        } else {
          Logger.w("Cover download from $url is not an image")
          saved?.delete()
          null
        }
      } catch (e: IOException) {
        Logger.w(e, "Failed to save cover from $url")
        null
      }
    }
  }

  private fun File.isDecodableImage(): Boolean {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, options)
    return options.outWidth > 0 && options.outHeight > 0
  }
}

private suspend fun Call.await(): Response {
  return suspendCancellableCoroutine { continuation ->
    enqueue(
      object : Callback {
        override fun onResponse(
          call: Call,
          response: Response,
        ) {
          continuation.resume(response)
        }

        override fun onFailure(
          call: Call,
          e: IOException,
        ) {
          if (continuation.isCancelled) return
          continuation.resumeWithException(e)
        }
      },
    )
    continuation.invokeOnCancellation {
      try {
        cancel()
      } catch (ignored: Throwable) {
      }
    }
  }
}
