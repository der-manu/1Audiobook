package voice.folderPicker.selectType

internal enum class FolderMode {
  Audiobooks,
  SingleBook,
  Authors,

  /** Alles unter dem Ordner automatisch erkennen und vor dem Hinzufuegen in einer Vorschau bearbeiten. */
  Library,
}
