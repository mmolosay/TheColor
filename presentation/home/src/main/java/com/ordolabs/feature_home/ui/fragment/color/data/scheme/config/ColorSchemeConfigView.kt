package com.ordolabs.feature_home.ui.fragment.color.data.scheme.config

import io.github.mmolosay.presentation.model.color.data.ColorSchemeRequest

interface ColorSchemeConfigView {

    /**
     * Currently applied to UI [ColorSchemeRequest.Config].
     */
    val appliedConfig: ColorSchemeRequest.Config?

    /**
     * Populates color scheme config `View` UI with current `config`.
     *
     * @return applied `config`.
     */
    fun applyCurrentConfig(): ColorSchemeRequest.Config?
}