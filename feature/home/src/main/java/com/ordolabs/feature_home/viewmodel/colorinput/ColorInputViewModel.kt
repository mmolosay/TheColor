package com.ordolabs.feature_home.viewmodel.colorinput

import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.model.color.ColorPrototype
import com.ordolabs.thecolor.model.color.toHex
import com.ordolabs.thecolor.model.color.toRgb
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.setEmpty
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.asStateFlow

class ColorInputViewModel : BaseViewModel() {

    private val _colorHex = MutableStateResourceFlow<ColorPrototype.Hex>(Resource.empty())
    val colorHex = _colorHex.asStateFlow()

    private val _colorRgb = MutableStateResourceFlow<ColorPrototype.Rgb>(Resource.empty())
    val colorRgb = _colorRgb.asStateFlow()

    fun updateColorInput(color: Color) {
        updateHexInput(color)
        updateRgbInput(color)
    }

    fun clearColorInput() {
        _colorHex.setEmpty()
        _colorRgb.setEmpty()
    }

    private fun updateHexInput(color: Color) {
        val hex = color.toHex()
        _colorHex.setSuccess(hex)
    }

    private fun updateRgbInput(color: Color) {
        val rgb = color.toRgb()
        _colorRgb.setSuccess(rgb)
    }
}