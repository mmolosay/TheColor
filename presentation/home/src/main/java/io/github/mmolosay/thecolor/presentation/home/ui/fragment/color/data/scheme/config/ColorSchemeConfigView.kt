package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.scheme.config

import io.github.mmolosay.thecolor.presentation.color.data.ColorSchemeRequest

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