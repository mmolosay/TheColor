package com.ordolabs.feature_home.viewmodel.colorinput

import androidx.lifecycle.viewModelScope
import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.model.color.ColorPrototype
import com.ordolabs.thecolor.model.color.toHex
import com.ordolabs.thecolor.model.color.toRgb
import com.ordolabs.thecolor.util.MutableCommandFlow
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.asCommand
import com.ordolabs.thecolor.util.ext.setEmpty
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.asStateFlow

/**
 * Contains colors obtained from input or ones to be set in it.
 *
 * `View` should call [updateColorPrototype], when color in UI is changed.
 *
 * `View` should call [updateCurrentColor], when there is new [Color], that should be populated
 * in all color input UIs.
 */
class ColorInputViewModel : BaseViewModel() {

    /**
     * `Flow` of [ColorPrototype] of currently active color input UI (such as `Fragment`).
     */
    private val _prototype = MutableStateResourceFlow<ColorPrototype>(Resource.empty())
    val prototype = _prototype.asStateFlow()

    private val _inputHex =
        MutableCommandFlow<ColorPrototype.Hex>()
    val inputHex = _inputHex.asCommand(viewModelScope)

    private val _inputRgb =
        MutableCommandFlow<ColorPrototype.Rgb>()
    val inputRgb = _inputRgb.asCommand(viewModelScope)

    // region Prototype

    /**
     * Updates current [color], displayed by `View`.
     *
     */
    fun updateColorPrototype(prototype: ColorPrototype) {
        _prototype.setSuccess(prototype)
    }

    private fun clearColorPrototype() {
        _prototype.setEmpty()

    }

    // endregion

    // region Input

    /**
     * Updates current [color], displayed by `View`.
     * It will be converted into [ColorPrototype] and set into appropriate `Flow`.
     * `View` should collect it and set in UI inputs.
     */
    fun updateCurrentColor(color: Color) {
        updateHexInput(color)
        updateRgbInput(color)
        updateColorPrototype(color.toHex())
    }

    fun clearColorInput() {
        _inputHex.setEmpty()
        _inputRgb.setEmpty()
        clearColorPrototype()
    }

    private fun updateHexInput(color: Color) {
        val hex = color.toHex()
        _inputHex.setSuccess(hex)
    }

    private fun updateRgbInput(color: Color) {
        val rgb = color.toRgb()
        _inputRgb.setSuccess(rgb)
    }

    private fun isInputEmpty(): Boolean =
        (_inputHex.value.isEmpty && _inputRgb.value.isEmpty)

    // endregion
}