package com.ordolabs.feature_home.ui.fragment.color.data.scheme.config

import com.ordolabs.thecolor.model.color.data.ColorSchemeRequest

/**
 * Interface for parent (ancestor) `View` of color scheme config `View`.
 * `View`, implementing this interface, would handle all actions from color scheme config `View`.
 */
interface ColorSchemeConfigParent {
    fun onCurrentConfigChanged(current: ColorSchemeRequest.Config)
}