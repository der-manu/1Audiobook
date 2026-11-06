package voice.app.features.bookOverview

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.widget.ImageView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import coil.size.Size
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.customview.customView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import voice.app.databinding.DialogCoverEditBinding
import voice.app.injection.appComponent
import voice.app.misc.conductor.context
import voice.app.scanner.CoverSaver
import voice.common.BookId
import voice.common.conductor.DialogController
import voice.common.parcelable
import voice.data.repo.BookRepository
import voice.logging.core.Logger
import javax.inject.Inject
import kotlin.math.roundToInt
import voice.common.R as CommonR
import voice.strings.R as StringsR

private const val NI_ARGS = "ni#bundle"

class EditCoverDialogController(bundle: Bundle) : DialogController(bundle) {

  constructor(args: Arguments) : this(
    Bundle().apply {
      putParcelable(NI_ARGS, args)
    },
  )

  @Inject
  lateinit var repo: BookRepository

  @Inject
  lateinit var coverSaver: CoverSaver

  @SuppressLint("InflateParams")
  override fun onCreateDialog(savedViewState: Bundle?): Dialog {
    appComponent.inject(this)

    val binding = DialogCoverEditBinding.inflate(activity!!.layoutInflater)
    val arguments = args.parcelable<Arguments>(NI_ARGS)!!

    binding.cropOverlay.selectionOn = true
    binding.coverImage.load(arguments.coverUri)

    val dialog = MaterialDialog(activity!!).apply {
      customView(view = binding.root)
      title(StringsR.string.cover)
      positiveButton(StringsR.string.dialog_confirm)
    }

    // use a click listener so the dialog stays open till the image was saved
    dialog.getActionButton(WhichButton.POSITIVE).setOnClickListener {
      // Kopie: der Bereich gehoert dem Overlay und darf hier nicht veraendert werden.
      val selection = Rect(binding.cropOverlay.selectedRect)
      if (selection.isEmpty) {
        dismissDialog()
        return@setOnClickListener
      }
      onCreateViewScope?.launch {
        val saved = try {
          cropAndSave(arguments, binding.coverImage, selection)
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          Logger.w(e, "Saving the cover failed")
          false
        }
        if (saved) {
          dismissDialog()
        } else {
          // Der Dialog bleibt offen, und der Nutzer erfaehrt, dass etwas schiefging.
          Toast.makeText(context, CommonR.string.cover_save_failed, Toast.LENGTH_LONG).show()
        }
      }
    }

    return dialog
  }

  /**
   * Rechnet die Auswahl vom Bildschirm auf das Originalbild um. Die Anzeige skaliert das Bild (und setzt es ggf.
   * mittig), deshalb wird die Bildmatrix der Ansicht rueckwaerts angewendet, statt nur die Breiten zu vergleichen.
   * Der Bereich wird auf die Bildgrenzen begrenzt, damit das Zuschneiden nicht an Rundungen scheitert.
   */
  private suspend fun cropAndSave(
    arguments: Arguments,
    imageView: ImageView,
    viewRect: Rect,
  ): Boolean {
    val full = context.imageLoader
      .execute(
        ImageRequest.Builder(context)
          .data(arguments.coverUri)
          .size(Size.ORIGINAL)
          .allowHardware(false)
          .build(),
      )
      .drawable?.toBitmap() ?: return false

    val drawable = imageView.drawable ?: return false
    if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) return false

    val viewToDrawable = Matrix()
    if (!imageView.imageMatrix.invert(viewToDrawable)) return false
    val area = RectF(viewRect)
    viewToDrawable.mapRect(area)

    val scaleX = full.width.toFloat() / drawable.intrinsicWidth
    val scaleY = full.height.toFloat() / drawable.intrinsicHeight
    val left = (area.left * scaleX).roundToInt().coerceIn(0, full.width - 1)
    val top = (area.top * scaleY).roundToInt().coerceIn(0, full.height - 1)
    val right = (area.right * scaleX).roundToInt().coerceIn(left + 1, full.width)
    val bottom = (area.bottom * scaleY).roundToInt().coerceIn(top + 1, full.height)

    val cropped = Bitmap.createBitmap(full, left, top, right - left, bottom - top)
    coverSaver.save(arguments.bookId, cropped)
    return true
  }

  @Parcelize
  data class Arguments(
    val coverUri: Uri,
    val bookId: BookId,
  ) : Parcelable
}
