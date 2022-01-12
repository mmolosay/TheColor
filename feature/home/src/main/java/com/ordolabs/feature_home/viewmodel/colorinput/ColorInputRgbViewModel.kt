package com.ordolabs.feature_home.viewmodel.colorinput

import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.model.color.ColorRgb
import com.ordolabs.thecolor.model.color.toColorRgb
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.asStateFlow

class ColorInputRgbViewModel : BaseViewModel() {

    private val _colorRgb = MutableStateResourceFlow<ColorRgb>(Resource.empty())
    val colorRgb = _colorRgb.asStateFlow()

    fun updateColorInput(color: Color) {
        val rgb = color.toColorRgb()
        _colorRgb.setSuccess(rgb)
    }
}