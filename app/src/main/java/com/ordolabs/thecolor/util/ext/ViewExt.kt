package com.ordolabs.thecolor.util.ext

import android.animation.Animator
import android.content.Context
import android.graphics.Point
import android.text.Editable
import android.text.InputFilter
import android.util.Property
import android.view.View
import android.view.ViewAnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.core.animation.doOnEnd
import androidx.core.graphics.minus
import androidx.core.view.isVisible
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.google.android.material.textfield.TextInputLayout
import com.ordolabs.thecolor.util.AnimationUtils
import com.ordolabs.thecolor.util.AnimationUtils.CustomViewProperty

// region TextView

/**
 * Sets specified [text] in [TextView.setText] and makes view visible,
 * if [text] is not [CharSequence.isNullOrEmpty].
 * Sets visibility to [View.GONE] otherwise.
 *
 * @return whether has meaningful text or not.
 */
fun TextView.setTextOrGone(text: CharSequence?): Boolean {
    val hasText = !text.isNullOrEmpty()
    this.isVisible = hasText
    if (hasText) {
        this.text = text
    }
    return hasText
}

/**
 * Performs [TextView.setTextOrGone], but __only hides__ [views].
 */
fun TextView.setTextOrGoneWith(text: CharSequence?, vararg views: View): Boolean {
    val hasText = !text.isNullOrEmpty()
    this.isVisible = hasText
    if (hasText) {
        this.text = text
    } else {
        views.forEach { it.isVisible = false }
    }
    return hasText
}

// endregion

// region TextInputLayout and EditText

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

fun EditText.hideSoftInput(): Boolean {
    val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    manager?.hideSoftInputFromWindow(this.windowToken, 0) ?: return false
    return true
}

// endregion

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

// region Animation

/**
 * Returns [SpringAnimation], associated with `this` view or a new one.
 * Note, that if returned animation is not a newly created, it would has all
 * listeners from the past.
 *
 * @see [View.createSpringAnimation]
 */
fun View.spring(
    property: DynamicAnimation.ViewProperty,
    damping: Float = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY,
    stiffness: Float = 500f // MEDIUM == 1500, LOW == 200
): SpringAnimation {
    val key = AnimationUtils.getSpringPropertyKey(property)
    return this.getTag(key) as? SpringAnimation
        ?: this.createSpringAnimation(property, damping, stiffness).also {
            this.setTag(key, it)
        }
}

/**
 * Returns newly created [SpringAnimation].
 */
fun View.createSpringAnimation(
    property: DynamicAnimation.ViewProperty,
    damping: Float = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY,
    stiffness: Float = 500f // MEDIUM == 1500, LOW == 200
): SpringAnimation {
    val animation = SpringAnimation(this, property)
    animation.spring = SpringForce().apply {
        this.dampingRatio = damping
        this.stiffness = stiffness
    }
    return animation
}

/**
 * Binds specified [animator] to `this` View, if there is no existing `Animator` bound to it.
 * If there is one, will do nothing.
 * If [animator] was bound, it will be unbound from `this` View, once it first ends.
 *
 * @return Already bound to `this` View `Animator`, if there is one, or [animator] otherwise.
 * @see View.bindPropertyAnimator
 */
@Suppress("UNCHECKED_CAST")
fun <T : Animator> View.propertyAnimator(@IdRes propertyKey: Int, animator: T): T =
    this.getTag(propertyKey) as? T
        ?: animator.also {
            this.setTag(propertyKey, it)
            it.doOnEnd {
                this.setTag(propertyKey, null)
            }
        }

fun <T : Animator> View.propertyAnimator(property: Property<*, *>, animator: T): T {
    val key = AnimationUtils.getViewPropertyKey(property)
    return this.propertyAnimator(key, animator)
}

fun <T : Animator> View.propertyAnimator(property: CustomViewProperty, animator: T): T {
    val key = property.key
    return this.propertyAnimator(key, animator)
}

/**
 * Retrieves bounded to `this` View `Animator` or `null`, if there is no such.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Animator> View.propertyAnimatorOrNull(@IdRes propertyKey: Int): T? =
    this.getTag(propertyKey) as? T

fun <T : Animator> View.propertyAnimatorOrNull(property: Property<*, *>): T? {
    val key = AnimationUtils.getViewPropertyKey(property)
    return this.propertyAnimatorOrNull(key)
}

fun <T : Animator> View.propertyAnimatorOrNull(property: CustomViewProperty): T? {
    val key = property.key
    return this.propertyAnimatorOrNull(key)
}

/**
 * Binds specified [animator] to `this` View.
 * In case there is an existing one, it will be overwritten.
 *
 * @return `Animator` that was overwritten, or `null` if there was no such.
 */
fun View.bindPropertyAnimator(@IdRes propertyKey: Int, animator: Animator): Animator? {
    val current = this.propertyAnimatorOrNull<Animator>(propertyKey)
    this.setTag(propertyKey, animator)
    animator.doOnEndOnce {
        this.setTag(propertyKey, null)
    }
    return current
}

fun View.bindPropertyAnimator(property: CustomViewProperty, animator: Animator): Animator? {
    val key = property.key
    return this.bindPropertyAnimator(key, animator)
}

/**
 * Returns newly created circular reveal animation.
 */
fun View.createCircularRevealAnimation(
    cx: Int = width / 2,
    cy: Int = height / 2,
    sr: Float = 0f,
    er: Float = AnimationUtils.getCircularRevealMaxRadius(this, cx, cy)
): Animator {
    return ViewAnimationUtils.createCircularReveal(this, cx, cy, sr, er)
}

fun View.createCircularRevealAnimation(
    reveal: Boolean,
    cx: Int = width / 2,
    cy: Int = height / 2,
    sr: Float = 0f,
    er: Float = AnimationUtils.getCircularRevealMaxRadius(this, cx, cy)
): Animator {
    return if (reveal) {
        this.createCircularRevealAnimation(cx, cy, sr, er)
    } else {
        this.createCircularRevealAnimation(cx, cy, er, sr)
    }
}

// endregion
