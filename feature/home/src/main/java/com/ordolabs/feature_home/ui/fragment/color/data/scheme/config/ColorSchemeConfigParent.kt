package com.ordolabs.feature_home.ui.fragment.color.data.scheme.config

import io.github.mmolosay.presentation.model.color.data.ColorSchemeRequest

/**
 * Interface for parent (ancestor) `View` of color scheme config `View`.
 * `View`, implementing this interface, would handle all actions from color scheme config `View`.
 */
interface ColorSchemeConfigParent {
    /**
     * Being called when UI of color scheme config `View` was changed,
     * so now it represents new [ColorSchemeRequest.Config].
     *
     * @param applied presently applied config.
     * @param current new config, determined by UI.
     */
    fun onCurrentConfigChanged(
        applied: ColorSchemeRequest.Config,
        current: ColorSchemeRequest.Config
    )
}