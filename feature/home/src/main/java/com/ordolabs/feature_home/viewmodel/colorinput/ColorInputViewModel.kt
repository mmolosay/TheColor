package com.ordolabs.feature_home.viewmodel.colorinput

import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.model.color.ColorHex
import com.ordolabs.thecolor.model.color.ColorRgb
import com.ordolabs.thecolor.model.color.toColorHex
import com.ordolabs.thecolor.model.color.toColorRgb
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.asStateFlow

class ColorInputViewModel : BaseViewModel() {

    private val _colorHex = MutableStateResourceFlow<ColorHex>(Resource.empty())
    val colorHex = _colorHex.asStateFlow()

    private val _colorRgb = MutableStateResourceFlow<ColorRgb>(Resource.empty())
    val colorRgb = _colorRgb.asStateFlow()

    fun updateColorInput(color: Color) {
        updateHexInput(color)
        updateRgbInput(color)
    }

    private fun updateHexInput(color: Color) {
        val hex = color.toColorHex()
        _colorHex.setSuccess(hex)
    }

    private fun updateRgbInput(color: Color) {
        val rgb = color.toColorRgb()
        _colorRgb.setSuccess(rgb)
    }
}