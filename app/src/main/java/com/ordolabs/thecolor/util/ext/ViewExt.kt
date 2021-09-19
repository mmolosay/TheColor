package com.ordolabs.thecolor.util.ext

import android.animation.Animator
import android.content.Context
import android.graphics.Point
import android.text.Editable
import android.text.InputFilter
import android.view.View
import android.view.ViewAnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.graphics.minus
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import com.google.android.material.textfield.TextInputLayout
import com.ordolabs.thecolor.util.AnimationUtils
import kotlin.math.hypot

/* TextInputLayout and EditText */

fun TextInputLayout.getText(): Editable? {
    return this.editText?.text
}

fun TextInputLayout.getTextString(): String? {
    return this.editText?.text?.toString()
}

fun TextInputLayout.hideSoftInput(): Boolean {
    return this.editText?.hideSoftInput() ?: false
}

fun EditText.addFilters(vararg filters: InputFilter) {
    val updated = this.filters.toMutableList().apply { addAll(filters) }
    this.filters = updated.toTypedArray()
}

fun EditText.hideSoftInput(): Boolean {
    val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    manager?.hideSoftInputFromWindow(this.windowToken, 0) ?: return false
    return true
}

/* Location */

/**
 * Computes coordinates of `this` view relative to specified [parent].
 *
 * @return [Point] with coordinates or `null`, if the view has no such parent in hierarchy.
 */
fun View.getLocationInParent(parent: View?): Point? {
    parent ?: return null
    var p: View = this
    var top = this.top
    var left = this.left
    while (true) {
        p = p.parent as? View ?: return null
        if (p == parent) {
            return Point(left, top)
        } else {
            top += p.top
            left += p.left
        }
    }
}

fun View.getDistanceInParent(to: View?, parent: View?): Point? {
    to ?: return null
    val toLocation = to.getLocationInParent(parent) ?: return null
    val thisLocation = this.getLocationInParent(parent) ?: return null
    return toLocation - thisLocation
}

fun View.getBottomVisibleInParent(parent: View?): Int? {
    parent ?: return null
    val location = this.getLocationInParent(parent) ?: return null
    val absBottom = location.y + this.height
    val clipped = absBottom - parent.height
    return if (clipped > 0) this.height - clipped else this.height
}

/* Animation */

fun View.spring(property: DynamicAnimation.ViewProperty): SpringAnimation {
    val key = AnimationUtils.getSpringPropertyKey(property)
    return this.getTag(key) as? SpringAnimation
        ?: SpringAnimation(this, property).also {
            this.setTag(key, it)
        }
}

fun View.circularRevealAnimation(
    cx: Int = width / 2,
    cy: Int = height / 2,
    sr: Float = 0f,
    er: Float = kotlin.run {
        val x = (if (cx >= width / 2) 0 else width).toFloat()
        val y = (if (cy >= height / 2) 0 else height).toFloat()
        hypot(x - cx, y - cy)
    }
): Animator {
    val key = AnimationUtils.getCircularRevealKey()
    return this.getTag(key) as? Animator
        ?: ViewAnimationUtils.createCircularReveal(this, cx, cy, sr, er).also {
            this.setTag(key, it)
        }
}
