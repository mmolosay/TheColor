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

/**
 * Contains colors obtained from input or ones to be set in it.
 *
 *
 * `View` should call [updateColorOutput], when there is new [ColorPrototype] collected.
 *
 * `View` should call [updateColorInput], when there is new valid [Color], that should be populated
 * in all color input UIs.
 */
class ColorInputViewModel : BaseViewModel() {

    // TODO: store one ColorPrototype
    private val _prototypeHex = MutableStateResourceFlow<ColorPrototype.Hex>(Resource.empty())
    val prototypeHex = _prototypeHex.asStateFlow()

    private val _prototypeRgb = MutableStateResourceFlow<ColorPrototype.Rgb>(Resource.empty())
    val prototypeRgb = _prototypeRgb.asStateFlow()

    private val _colorHex = MutableStateResourceFlow<ColorPrototype.Hex>(Resource.empty())
    val colorHex = _colorHex.asStateFlow()

    private val _colorRgb = MutableStateResourceFlow<ColorPrototype.Rgb>(Resource.empty())
    val colorRgb = _colorRgb.asStateFlow()

    // region Output

    fun updateColorOutput(prototype: ColorPrototype) =
        when (prototype) {
            is ColorPrototype.Hex -> updateColorOutput(prototype)
            is ColorPrototype.Rgb -> updateColorOutput(prototype)
        }

    private fun updateColorOutput(prototype: ColorPrototype.Hex) {
        _prototypeHex.setSuccess(prototype)
    }

    private fun updateColorOutput(prototype: ColorPrototype.Rgb) {
        _prototypeRgb.setSuccess(prototype)
    }

    // endregion

    // region Input

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

    // endregion
}