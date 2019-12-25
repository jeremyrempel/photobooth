package com.jeremyrempel.android.photobooth

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import java.io.File

class MainActivity : AppCompatActivity(), PhotoBoothView {

    private lateinit var drawingView: DrawingView
    private lateinit var presenter: PhotoBoothPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = PhotoBoothPresenter(
            this as PhotoBoothView,
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        )

        setContentView(R.layout.activity_main)

        initClickListeners()

        drawingView = findViewById(R.id.view_drawing)
        onNewColorSelected(getColor(DEFAULT_COLOR))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_TAKE_PHOTO -> {
                presenter.onNewPhoto()
            }
        }
    }

    /**
     * Setup the onclick listeners
     */
    private fun initClickListeners() {
        findViewById<AppCompatImageButton>(R.id.btn_take_photo).setOnClickListener { presenter.takePhoto() }
        findViewById<AppCompatImageButton>(R.id.btn_new).setOnClickListener { presenter.clear() }
        findViewById<AppCompatImageButton>(R.id.btn_undo).setOnClickListener {
            presenter.undo()
        }
        findViewById<AppCompatImageButton>(R.id.btn_save).setOnClickListener {
            presenter.save(drawingView.getLayers())
        }

        // setup color picker actions
        findViewById<AppCompatImageButton>(R.id.btn_color_picker).setOnClickListener {
            showHideColorDialog()
        }

        findViewById<ImageView>(R.id.color_pick_black).setOnClickListener {
            onNewColorSelected(getColor(android.R.color.black))
        }

        findViewById<ImageView>(R.id.color_pick_blue).setOnClickListener {
            onNewColorSelected(getColor(android.R.color.holo_blue_dark))
        }

        findViewById<ImageView>(R.id.color_pick_red).setOnClickListener {
            onNewColorSelected(getColor(android.R.color.holo_red_dark))
        }

        findViewById<ImageView>(R.id.color_pick_green).setOnClickListener {
            onNewColorSelected(getColor(android.R.color.holo_green_dark))
        }

        findViewById<ImageView>(R.id.color_pick_orange).setOnClickListener {
            onNewColorSelected(getColor(android.R.color.holo_orange_dark))
        }

        findViewById<ImageView>(R.id.color_pick_blue).setOnClickListener {
            onNewColorSelected(getColor(android.R.color.holo_blue_dark))
        }

        findViewById<ImageView>(R.id.color_pick_purple).setOnClickListener {
            onNewColorSelected(getColor(android.R.color.holo_purple))
        }
    }

    override fun showPhoto(bitmap: Bitmap) {
        val imageView = findViewById<ImageView>(R.id.img_photo)
        imageView.setImageBitmap(bitmap)
        drawingView.reset()
        drawingView.bringToFront()
    }

    private fun onError(error: Throwable) {
        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
    }

    override fun requestCamera(tempFile: File) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {

                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.jeremyrempel.android.photo.fileprovider",
                    tempFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }

    override fun onUndo() {
        if (!drawingView.undo()) {
            onError(Exception(getString(R.string.undo_fail)))
        }
    }

    override fun showHideColorDialog() {
        val grid = findViewById<View>(R.id.view_color_grid)
        grid.visibility = if (grid.isVisible) View.GONE else View.VISIBLE
        grid.bringToFront()
    }

    override fun onNewColorSelected(@ColorInt newColor: Int) {
        findViewById<AppCompatImageButton>(R.id.btn_color_picker).setColorFilter(newColor)
        findViewById<View>(R.id.view_color_grid).visibility = View.GONE
        drawingView.setColor(newColor)
    }

    override fun onClear() {
        val imageView = findViewById<ImageView>(R.id.img_photo)
        imageView.setImageBitmap(null)
        drawingView.reset()
    }

    override fun onSaveSucess(location: String) {
        val result = getString(R.string.file_save, location)
        Toast.makeText(applicationContext, result, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val REQUEST_TAKE_PHOTO = 1
        const val DEFAULT_COLOR = android.R.color.black
    }
}
