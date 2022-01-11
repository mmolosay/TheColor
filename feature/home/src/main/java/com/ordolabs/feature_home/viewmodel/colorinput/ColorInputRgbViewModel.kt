package com.ordolabs.feature_home.viewmodel.colorinput

import com.ordolabs.thecolor.model.color.ColorPreview
import com.ordolabs.thecolor.model.color.toColorInputRgbPresentation
import com.ordolabs.thecolor.model.colorinput.ColorInputRgbPresentation
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.asStateFlow

class ColorInputRgbViewModel : BaseViewModel() {

    private val _colorRgb = MutableStateResourceFlow<ColorInputRgbPresentation>(Resource.empty())
    val colorRgb = _colorRgb.asStateFlow()

    fun updateColorInput(input: ColorPreview) {
        val color = input.toColorInputRgbPresentation()
        _colorRgb.setSuccess(color)
    }
}