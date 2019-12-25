package com.jeremyrempel.android.photobooth

import android.graphics.Bitmap
import androidx.annotation.ColorInt
import java.io.File

interface PhotoBoothView {
    fun requestCamera(tempFile: File)
    fun showPhoto(bitmap: Bitmap)
    fun showHideColorDialog()
    fun onNewColorSelected(@ColorInt newColor: Int)
    fun onClear()
    fun onUndo()
    fun onSaveSucess(location: String)
}