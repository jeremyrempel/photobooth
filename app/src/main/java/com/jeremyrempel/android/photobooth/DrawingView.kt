package com.jeremyrempel.android.photobooth

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import java.util.*
import kotlin.math.abs

class DrawingView(context: Context, attributes: AttributeSet) : View(context, attributes) {
    private var compositeLayer: Bitmap? = null
    private val currentCanvas = Canvas()
    private val currentPath: Path = Path()
    private val currentPaint = Paint(Paint.DITHER_FLAG)

    private var currentTouchX = 0f
    private var currentTouchY = 0f

    private val layers: Deque<Bitmap> = LinkedList<Bitmap>()

    /**
     * @return true if layer was removed, false if not
     */
    fun undo(): Boolean {
        val result = if (layers.isNotEmpty()) {
            layers.removeFirst()
            true
        } else {
            false
        }

        redraw()

        return result
    }

    fun setColor(@ColorInt color: Int) {
        currentPaint.color = color
    }

    fun reset() {
        layers.clear()
        redraw()
    }

    fun getLayers(): Array<Bitmap> = layers.reversed().toTypedArray()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // draw composite, current layer and then path
        compositeLayer?.let {
            canvas.drawBitmap(it, 0f, 0f, currentPaint)
        }

        if (layers.isNotEmpty()) {
            canvas.drawBitmap(layers.first, 0f, 0f, currentPaint)
        }

        canvas.drawPath(currentPath, currentPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }
        return true
    }

    private fun buildCompositeLayer(): Bitmap {

        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        layers.descendingIterator().forEach {
            canvas.drawBitmap(it, 0f, 0f, currentPaint)
        }

        return bmp
    }

    private fun touchStart(x: Float, y: Float) {
        newLayer()
        currentPath.reset()
        currentPath.moveTo(x, y)
        currentTouchX = x
        currentTouchY = y
    }

    private fun touchMove(x: Float, y: Float) {
        val dx = abs(x - currentTouchX)
        val dy = abs(y - currentTouchY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            currentPath.quadTo(
                currentTouchX,
                currentTouchY,
                (x + currentTouchX) / 2,
                (y + currentTouchY) / 2
            )
            currentTouchX = x
            currentTouchY = y
        }
    }

    private fun touchUp() {
        currentCanvas.drawPath(currentPath, currentPaint)
        compositeLayer = buildCompositeLayer()
        invalidate()
    }

    /**
     * Rebuild all layers and redraw
     */
    private fun redraw() {
        currentPath.reset()
        compositeLayer = buildCompositeLayer()
        currentCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    private fun newLayer(): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            currentCanvas.setBitmap(this)
            layers.addFirst(this)
        }
    }

    companion object {
        private const val TOUCH_TOLERANCE = 4f
        private const val BRUSH_WIDTH = 40f
    }

    init {
        currentPaint.isAntiAlias = true
        currentPaint.isDither = true
        currentPaint.style = Paint.Style.STROKE
        currentPaint.strokeJoin = Paint.Join.ROUND
        currentPaint.strokeCap = Paint.Cap.ROUND
        currentPaint.strokeWidth = BRUSH_WIDTH
    }
}