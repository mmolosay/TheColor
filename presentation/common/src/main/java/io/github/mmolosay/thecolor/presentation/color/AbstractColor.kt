package io.github.mmolosay.thecolor.presentation.color

import android.os.Parcelable

/**
 * Empty class, that indicates that class, inheriting it,
 * is a presentation of color of some color space.
 * All inheritors __must__ override [equals] method.
 */
sealed class AbstractColor : Parcelable {

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other !is AbstractColor) return false
        val thisColor = Color.from(this) ?: return false
        val otherColor = Color.from(other) ?: return false
        return (thisColor == otherColor)
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}