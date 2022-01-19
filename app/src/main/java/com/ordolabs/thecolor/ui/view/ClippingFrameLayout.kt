package com.ordolabs.thecolor.ui.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.doOnLayout

class ClippingFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var backgroundBitmap: Bitmap? = null

    private var childrenBitmap: Bitmap? = null
    private var childrenCanvas: Canvas? = null

    private val clippingPaint = Paint().apply {
        isAntiAlias = true
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }

    override fun setBackground(background: Drawable?) {
        super.setBackground(background)
        doOnLayout {
            setBackgroundBitmap(background, measuredWidth, measuredHeight)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setBackgroundBitmap(background, w, h)
        setChildrenBitmap(w, h)
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
//        childrenCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
//        childrenBitmap?.eraseColor(Color.TRANSPARENT)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
//        childrenBitmap?.eraseColor(Color.TRANSPARENT)
    }

    override fun dispatchDraw(canvas: Canvas?) {
        canvas ?: return super.dispatchDraw(canvas)
        val childrenCanvas = childrenCanvas
        val childrenBitmap = childrenBitmap
        childrenBitmap?.eraseColor(Color.TRANSPARENT)
        if (childrenCanvas != null && childrenBitmap != null) {
            // draw children on clean canvas
            super.dispatchDraw(childrenCanvas)
            // clip children with background drawable
            backgroundBitmap?.let {
                childrenCanvas.drawBitmap(it, 0f, 0f, clippingPaint)
            }
            // draw clipped children on parameter canvas
            canvas.drawBitmap(childrenBitmap, 0f, 0f, /*paint*/ null)
        } else {
            super.dispatchDraw(canvas)
        }
    }

    override fun drawChild(canvas: Canvas?, child: View?, drawingTime: Long): Boolean {
//        if (indexOfChild(child) == 0)
        return super.drawChild(canvas, child, drawingTime)
    }

    private fun setBackgroundBitmap(
        background: Drawable?,
        width: Int,
        height: Int
    ) {
        backgroundBitmap?.recycle()
        this.backgroundBitmap =
            background?.toBitmap(
                width = width,
                height = height
            )
    }

    private fun setChildrenBitmap(width: Int, height: Int) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        childrenBitmap?.recycle()
        this.childrenBitmap = bitmap
        this.childrenCanvas = Canvas(bitmap)
    }
}