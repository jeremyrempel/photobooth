package com.jeremyrempel.android.photobooth

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PhotoBoothPresenter(private val view: PhotoBoothView, private val storageDir: File) {
    var currentPhotoPath: File? = null

    fun undo() {
        view.onUndo()
    }

    fun save(layers: Array<Bitmap>) {
        // todo this should be on a bg thread

        // make a dir
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss")
            .format(Date())
        val saveDir = File(storageDir, timeStamp)
        saveDir.mkdirs()

        // save user photo as layer 0
        if (currentPhotoPath != null) {
            val photoFile = File(saveDir, "0.png")
            FileOutputStream(photoFile).use { out ->
                val bmp = BitmapFactory.decodeFile(currentPhotoPath!!.absolutePath)
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }

        // save subsequent layers
        layers.forEachIndexed { idx, bmp ->
            val file = File(saveDir, "${idx + 1}.png")
            FileOutputStream(file).use { out ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }

        // notify user
        view.onSaveSucess(saveDir.toString())
    }

    fun onNewPhoto() {
        // save rotated photo
        // todo this should be on a bg thread
        val photoPath = requireNotNull(currentPhotoPath)

        val exif = ExifInterface(photoPath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        val originalBmp =
            BitmapFactory.decodeFile(photoPath.absolutePath)
        val rotatedBmp = rotateBitmap(originalBmp, orientation)

        FileOutputStream(photoPath).use { out ->
            rotatedBmp.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        view.showPhoto(rotatedBmp)
    }

    fun clear() {
        currentPhotoPath?.delete()
        view.onClear()
    }

    fun takePhoto(tempFileTest: File? = null) {
        val tempFile = tempFileTest ?: createTempImageFile()
        view.requestCamera(tempFile)
        currentPhotoPath = tempFile
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_NORMAL -> return bitmap
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        ).also { bitmap.recycle() }
    }

    @Throws(IOException::class)
    private fun createTempImageFile(suffix: String = "jpg"): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss")
            .format(Date())
        return File.createTempFile(
            "photo_${timeStamp}_",
            ".$suffix",
            storageDir
        ).apply {
            deleteOnExit()
        }
    }
}