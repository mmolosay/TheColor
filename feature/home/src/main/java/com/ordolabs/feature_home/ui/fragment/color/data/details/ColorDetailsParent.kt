package com.ordolabs.feature_home.ui.fragment.color.data.details

import com.ordolabs.thecolor.model.color.Color

/**
 * Interface for parent `View` of color details `View`, which would search for it in its ancestors.
 */
interface ColorDetailsParent {
    fun onExactColorClick(exact: Color)
}