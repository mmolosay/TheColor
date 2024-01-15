package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.scheme.editor

import io.github.mmolosay.thecolor.presentation.color.data.ColorSchemeRequest

/**
 * Interface for parent (ancestor) `View` of color scheme editor `View`.
 * `View`, implementing this interface, would handle all actions from color scheme editor `View`.
 */
interface ColorSchemeEditorParent {
    fun dispatchColorSchemeConfig(config: ColorSchemeRequest.Config)
}