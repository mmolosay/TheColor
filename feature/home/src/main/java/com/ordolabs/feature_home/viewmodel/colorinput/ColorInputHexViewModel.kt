package com.ordolabs.feature_home.viewmodel.colorinput

import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.model.color.ColorHex
import com.ordolabs.thecolor.model.color.toColorHex
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.asStateFlow

class ColorInputHexViewModel : BaseViewModel() {

    private val _colorHex = MutableStateResourceFlow<ColorHex>(Resource.empty())
    val colorHex = _colorHex.asStateFlow()

    fun updateColorInput(color: Color) {
        val hex = color.toColorHex()
        _colorHex.setSuccess(hex)
    }
}