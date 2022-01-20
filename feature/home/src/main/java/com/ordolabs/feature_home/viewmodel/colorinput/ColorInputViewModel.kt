package com.ordolabs.feature_home.viewmodel.colorinput

import com.ordolabs.thecolor.model.color.ColorInput
import com.ordolabs.thecolor.model.color.ColorPreview
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
 * `View` should call [updateColorPrototype], when color in UI is changed.
 *
 * `View` should call [updateCurrentColor], when there is new [ColorPreview], that should be populated
 * in all color input UIs.
 */
class ColorInputViewModel : BaseViewModel() {

    private val _prototype = MutableStateResourceFlow<ColorPrototype>(Resource.empty())
    val prototype = _prototype.asStateFlow()

    private val _inputHex =
        MutableStateResourceFlow<ColorInput<ColorPrototype.Hex>>(Resource.empty())
    val inputHex = _inputHex.asStateFlow()

    private val _inputRgb =
        MutableStateResourceFlow<ColorInput<ColorPrototype.Rgb>>(Resource.empty())
    val inputRgb = _inputRgb.asStateFlow()

    // region Prototype

    fun updateColorPrototype(prototype: ColorPrototype) {
        _prototype.setSuccess(prototype)
    }

    private fun clearColorPrototype() {
        _prototype.setEmpty()

    }

    // endregion

    // region Input

    fun updateCurrentColor(preview: ColorPreview) {
        updateHexInput(preview)
        updateRgbInput(preview)
        updateColorPrototype(preview.toHex())
    }

    fun clearColorInput() {
        _inputHex.setEmpty()
        _inputRgb.setEmpty()
        _prototype.setEmpty()
        clearColorPrototype()
    }

    private fun updateHexInput(preview: ColorPreview) {
        val hex = preview.toHex()
        val input = ColorInput(hex, forcePopulate = !preview.isUserInput)
        _inputHex.setSuccess(input)
    }

    private fun updateRgbInput(preview: ColorPreview) {
        val rgb = preview.toRgb()
        val input = ColorInput(rgb, forcePopulate = !preview.isUserInput)
        _inputRgb.setSuccess(input)
    }

    // endregion
}