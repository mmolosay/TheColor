package com.ordolabs.feature_home.ui.fragment.color.data.details

import com.ordolabs.thecolor.model.color.Color

/**
 * Interface for parent (ancestor) `View` of color details `View`.
 * `View`, implementing this interface, would handle all actions from color details `View`.
 */
interface ColorDetailsParent {
    fun onExactColorClick(exact: Color)
}