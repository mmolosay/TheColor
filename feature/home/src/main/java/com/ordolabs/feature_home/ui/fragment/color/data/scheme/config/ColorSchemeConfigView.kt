package com.ordolabs.feature_home.ui.fragment.color.data.scheme.config

import com.ordolabs.thecolor.model.color.data.ColorSchemeRequest

interface ColorSchemeConfigView {

    val appliedConfig: ColorSchemeRequest.Config?

    fun applyCurrentConfig(): ColorSchemeRequest.Config?
}