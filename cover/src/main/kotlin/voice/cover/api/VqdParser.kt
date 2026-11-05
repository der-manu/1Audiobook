package voice.cover.api

/** Das Such-Token steht je nach Seitenversion als vqd=4-123&, vqd="4-123", vqd='4-123' oder "vqd":"4-123" im Text. */
private val VQD = Regex("""vqd["']?\s*[=:]\s*["']?(\d+(?:-\d+)+)""")

/** Liest das Such-Token ("vqd") aus der Startseite der Suche. Null, wenn keins zu finden ist. */
internal fun parseVqd(page: String): String? = VQD.find(page)?.groupValues?.get(1)
