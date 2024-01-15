package io.github.mmolosay.thecolor.presentation.home.viewmodel.color.input

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.color.Color
import io.github.mmolosay.thecolor.presentation.color.ColorPrototype
import io.github.mmolosay.thecolor.presentation.color.toHex
import io.github.mmolosay.thecolor.presentation.color.toRgb
import io.github.mmolosay.thecolor.presentation.util.ext.shareOnceIn
import io.github.mmolosay.thecolor.utils.Resource
import io.github.mmolosay.thecolor.utils.empty
import io.github.mmolosay.thecolor.utils.success
import io.github.mmolosay.thecolor.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/**
 * Contains colors obtained from input or ones to be set in it.
 *
 * `View` should call [updateCurrentColor], when there is new [Color], that should be populated
 * in all color input UIs.
 */
@HiltViewModel
class ColorInputViewModel @Inject constructor() : BaseViewModel() {

    /**
     * `Flow` of [ColorPrototype] of currently active color input UI (such as `Fragment`).
     */

    private val _inputHex =
        MutableStateFlow<Resource<ColorPrototype.Hex>>(Resource.empty())
    val inputHex = _inputHex.shareOnceIn(viewModelScope)

    private val _inputRgb =
        MutableStateFlow<Resource<ColorPrototype.Rgb>>(Resource.empty())
    val inputRgb = _inputRgb.shareOnceIn(viewModelScope)

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
        _inputHex.value = Resource.empty()
        _inputRgb.value = Resource.empty()
    }

    private fun updateHexInput(color: Color) {
        val hex = color.toHex()
        _inputHex.value = Resource.success(hex)
    }

    private fun updateRgbInput(color: Color) {
        val rgb = color.toRgb()
        _inputRgb.value = Resource.success(rgb)
    }
}