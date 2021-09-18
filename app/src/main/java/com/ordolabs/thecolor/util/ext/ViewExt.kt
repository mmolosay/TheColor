package com.ordolabs.thecolor.util.ext

import android.graphics.Point
import android.text.Editable
import android.text.InputFilter
import android.view.View
import android.widget.EditText
import androidx.core.graphics.minus
import com.google.android.material.textfield.TextInputLayout

fun TextInputLayout.getText(): Editable? {
    return this.editText?.text
}

fun TextInputLayout.getTextString(): String? {
    return this.editText?.text?.toString()
}

fun EditText.addFilters(vararg filters: InputFilter) {
    val updated = this.filters.toMutableList().apply { addAll(filters) }
    this.filters = updated.toTypedArray()
}

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