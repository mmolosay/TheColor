package com.ordolabs.feature_home.viewmodel.colorinput

import androidx.lifecycle.viewModelScope
import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.model.color.ColorPrototype
import com.ordolabs.thecolor.model.color.toHex
import com.ordolabs.thecolor.model.color.toRgb
import com.ordolabs.thecolor.util.MutableCommandFlow
import com.ordolabs.thecolor.util.ext.asCommand
import com.ordolabs.thecolor.util.ext.setEmpty
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import javax.inject.Inject

/**
 * Contains colors obtained from input or ones to be set in it.
 *
 * `View` should call [updateCurrentColor], when there is new [Color], that should be populated
 * in all color input UIs.
 */
class ColorInputViewModel @Inject constructor() : BaseViewModel() {

    /**
     * `Flow` of [ColorPrototype] of currently active color input UI (such as `Fragment`).
     */

    private val _inputHex =
        MutableCommandFlow<ColorPrototype.Hex>()
    val inputHex = _inputHex.asCommand(viewModelScope)

    private val _inputRgb =
        MutableCommandFlow<ColorPrototype.Rgb>()
    val inputRgb = _inputRgb.asCommand(viewModelScope)

    /**
     * Updates current [color], displayed by `View`.
     * It will be converted into [ColorPrototype] and set into appropriate `Flow`.
     * `View` should collect it and set in UI inputs.
     */
    fun updateCurrentColor(color: Color) {
        updateHexInput(color)
        updateRgbInput(color)
    }

    fun clearColorInput() {
        _inputHex.setEmpty()
        _inputRgb.setEmpty()
    }

    private fun updateHexInput(color: Color) {
        val hex = color.toHex()
        _inputHex.setSuccess(hex)
    }

    private fun updateRgbInput(color: Color) {
        val rgb = color.toRgb()
        _inputRgb.setSuccess(rgb)
    }
}