package com.ordolabs.thecolor.ui.adapter.decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.children
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ordolabs.thecolor.R
import kotlin.math.roundToInt

/**
 * Draws [divider] only `between` [RecyclerView]'s items — doesn't draw after the last one.
 *
 * @param orientation either [LinearLayout.VERTICAL] or [LinearLayout.HORIZONTAL].
 */
class InBetweenDividerDecoration(
    context: Context,
    orientation: Int
) : RecyclerView.ItemDecoration() {

    // do not use solid colors
    var divider: Drawable? = AppCompatResources.getDrawable(context, R.drawable.bg_divider)
    var offsets: Rect = Rect(0, 0, 0, 0)

    private val orientation: Int = orientation
    private val bounds: Rect = Rect()

    constructor(context: Context, orientation: Int, offsets: Rect) : this(context, orientation) {
        this.offsets = offsets
    }

    /**
     * Changes [divider]'s drawable color to specified [color].
     *
     * @throws IllegalArgumentException if [divider] is neither
     * instance of [ColorDrawable], [GradientDrawable] or [ShapeDrawable]
     */
    fun setDividerColor(@ColorInt color: Int) {
        val d = divider ?: return
        when (d) {
            is ColorDrawable -> d.color = color
            is GradientDrawable -> d.setColor(color)
            is ShapeDrawable -> d.paint.color = color
            else -> throw IllegalArgumentException(
                "can not change color of this divider drawable"
            )
        }
    }

    override fun onDraw(c: Canvas, recycler: RecyclerView, state: RecyclerView.State) {
        when (orientation) {
            LinearLayout.VERTICAL -> drawVertical(c, recycler)
            LinearLayout.HORIZONTAL -> drawHorizontal(c, recycler)
            else -> throw IllegalArgumentException(
                "orientation must be either VERTICAL or HORIZONTAL"
            )
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        child: View,
        recycler: RecyclerView,
        state: RecyclerView.State
    ) {
        val d = divider
        if (d == null) {
            outRect.setEmpty()
            return
        }
        if (orientation == LinearLayout.VERTICAL) {
            outRect.set(0, 0, 0, d.intrinsicHeight)
        } else {
            outRect.set(0, 0, d.intrinsicWidth, 0)
        }
    }

    private fun drawVertical(c: Canvas, recycler: RecyclerView) {
        val divider = divider ?: return
        val children = recycler.children.toList().takeIf { it.size > 1 } ?: return
        val checkpoint = c.save()

        if (recycler.clipToPadding) {
            val left = recycler.paddingLeft + offsets.left
            val right = recycler.width - recycler.paddingRight - offsets.right
            val top = recycler.paddingTop + offsets.top
            val bottom = recycler.height - recycler.paddingBottom - offsets.bottom
            c.clipRect(left, top, right, bottom)
        }

        children.forEachIndexed { i, child ->
            if (isLastSpanItem(recycler, i)) return@forEachIndexed

            recycler.getDecoratedBoundsWithMargins(child, bounds)
            val left = bounds.left + child.translationX.roundToInt()
            val right = left + bounds.width()
            val bottom = bounds.bottom + child.translationY.roundToInt()
            val top = bottom - divider.intrinsicHeight

            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }

        c.restoreToCount(checkpoint)
    }

    // is not fully correct; should be reworked at moment of need
    private fun drawHorizontal(c: Canvas, recycler: RecyclerView) {
        val divider = divider ?: return
        val children = recycler.children.toList().takeIf { it.size > 1 } ?: return

        val checkpoint = c.save()
        val top: Int
        val bottom: Int

        if (recycler.clipToPadding) {
            top = recycler.paddingTop
            bottom = recycler.height - recycler.paddingBottom
            val left = recycler.paddingLeft
            val right = recycler.width - recycler.paddingRight
            c.clipRect(left, top, right, bottom)
        } else {
            top = 0
            bottom = recycler.height
        }

        children.forEach { child ->
            if (child == children.last()) return@forEach

            recycler.layoutManager?.getDecoratedBoundsWithMargins(child, bounds)
            val right = bounds.right + child.translationX.roundToInt()
            val left = right - divider.intrinsicWidth

            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }

        c.restoreToCount(checkpoint)
    }

    private fun isLastSpanItem(recycler: RecyclerView, childPos: Int): Boolean {
        return when (val lm = recycler.layoutManager) {
            is GridLayoutManager -> {
                val spanCount = lm.spanCount
                ((childPos + 1) % spanCount == 0)
            }
            is LinearLayoutManager -> {
                val child = recycler.getChildAt(childPos)
                val last = recycler.getChildAt(recycler.childCount - 1)
                (child == last)
            }
            else ->
                throw IllegalStateException("This layoutManager is not supported")
        }
    }
}
