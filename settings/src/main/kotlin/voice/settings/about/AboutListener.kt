package voice.settings.about

interface AboutListener {
  fun close()
  fun onUrlClick(url: String)
}
