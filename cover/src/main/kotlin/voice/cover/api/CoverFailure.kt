package voice.cover.api

import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/** Das Such-Token fehlte in der Antwort der Suchseite. */
internal class NoTokenException : IOException("No search token found")

/** Warum eine Suche scheiterte, in einer Form, die die Oberflaeche verstaendlich erklaeren kann. */
internal data class CoverFailure(
  val kind: Kind,
  val httpCode: Int = 0,
) {

  enum class Kind {
    /** Der Server hat die Anfrage mit einem HTTP-Fehler abgelehnt. */
    HTTP,

    /** Keine Verbindung oder Zeitueberschreitung. */
    OFFLINE,

    /** Die Antwort hatte ein unerwartetes Format. */
    UNEXPECTED,
  }

  companion object {
    fun from(error: Throwable): CoverFailure = when (error) {
      is HttpException -> CoverFailure(Kind.HTTP, error.code())
      is NoTokenException, is SerializationException -> CoverFailure(Kind.UNEXPECTED)
      is IOException -> CoverFailure(Kind.OFFLINE)
      else -> CoverFailure(Kind.UNEXPECTED)
    }
  }
}
