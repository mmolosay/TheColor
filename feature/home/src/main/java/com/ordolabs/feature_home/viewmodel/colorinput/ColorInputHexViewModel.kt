package com.ordolabs.feature_home.viewmodel.colorinput

import com.ordolabs.thecolor.model.color.ColorPreview
import com.ordolabs.thecolor.model.color.toColorInputHexPresentation
import com.ordolabs.thecolor.model.colorinput.ColorInputHexPresentation
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.asStateFlow

class ColorInputHexViewModel : BaseViewModel() {

    private val _colorHex = MutableStateResourceFlow<ColorInputHexPresentation>(Resource.empty())
    val colorHex = _colorHex.asStateFlow()

    fun updateColorInput(input: ColorPreview) {
        val presentation = input.toColorInputHexPresentation()
        _colorHex.setSuccess(presentation)
    }
}