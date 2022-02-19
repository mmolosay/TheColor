package com.ordolabs.feature_home.ui.fragment.color.data.scheme.editor

import com.ordolabs.thecolor.model.color.data.ColorSchemeRequest

/**
 * Interface for parent (ancestor) `View` of color scheme editor `View`.
 * `View`, implementing this interface, would handle all actions from color scheme editor `View`.
 */
interface ColorSchemeEditorParent {
    fun dispatchColorSchemeConfig(config: ColorSchemeRequest.Config)
}