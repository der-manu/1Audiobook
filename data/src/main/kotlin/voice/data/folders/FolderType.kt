package voice.data.folders

enum class FolderType {
  SingleFile,
  SingleFolder,
  Root,
  Author,

  /** Ein Wurzelordner; welche Unterordner Buecher sind, wurde in der Vorschau festgelegt. */
  Library,
}
