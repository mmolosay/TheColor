package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.details

import io.github.mmolosay.thecolor.presentation.color.Color

/**
 * Interface for parent (ancestor) `View` of color details `View`.
 * `View`, implementing this interface, would handle all actions from color details `View`.
 */
interface ColorDetailsParent {
    fun onExactColorClick(exact: Color)
}