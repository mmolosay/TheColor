package com.ordolabs.feature_home.viewmodel.colordata.scheme

import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.model.color.data.ColorSchemeRequest
import com.ordolabs.thecolor.viewmodel.BaseViewModel

/**
 * Stores [ColorSchemeRequest.Config] parameters and assembles new instances of `Config`.
 */
class ColorSchemeConfigViewModel : BaseViewModel() {

    var mode: ColorScheme.Mode = ColorScheme.Mode.DEFAULT

    fun assembleConfig() =
        ColorSchemeRequest.Config(
            mode.ordinal,
            8
        )
}