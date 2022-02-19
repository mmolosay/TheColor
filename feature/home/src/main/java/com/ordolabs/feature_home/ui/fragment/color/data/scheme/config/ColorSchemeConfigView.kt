package com.ordolabs.feature_home.ui.fragment.color.data.scheme.config

import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.model.color.data.ColorSchemeRequest

interface ColorSchemeConfigView {

    var mode: ColorScheme.Mode
    var sampleCount: Int
    var appliedConfig: ColorSchemeRequest.Config

    fun applyCurrentConfig(): ColorSchemeRequest.Config
}