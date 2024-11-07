package io.github.mmolosay.thecolor.presentation.impl

import android.graphics.Point
import android.view.View
import android.widget.ScrollView
import androidx.core.graphics.minus

// TODO: delete once implemented in new Compose UI
// region Location

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

fun View.getDistanceToViewInParent(to: View?, parent: View?): Point? {
    to ?: return null
    val toLocation = to.getLocationInParent(parent) ?: return null
    val thisLocation = this.getLocationInParent(parent) ?: return null
    return toLocation - thisLocation
}

/**
 * Calculates bottom of `this` view, that visible in specified [parent].
 *
 * @return [View.getHeight], if the view bottom is not clipped by [parent],
 * otherwise clipped position.
 *
 * @see getLocationInParent
 */
fun View.getBottomVisibleInParent(parent: View?): Int? {
    parent ?: return null
    val location = this.getLocationInParent(parent) ?: return null
    val absBottom = location.y + this.height
    val clipped = absBottom - parent.height
    return if (clipped > 0) this.height - clipped else this.height
}

fun View.getBottomVisibleInScrollParent(parent: ScrollView?): Int? {
    parent ?: return null
    val location = this.getLocationInParent(parent) ?: return null
    val absBottom = location.y + this.height
    val viewClippedBottom = parent.scrollY + parent.height
    return if (absBottom > viewClippedBottom) {
        viewClippedBottom - location.y
    } else {
        this.height
    }
}

// endregion