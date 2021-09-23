package com.ordolabs.feature_home.viewmodel

import androidx.lifecycle.viewModelScope
import com.ordolabs.domain.usecase.local.ValidateColorHexBaseUseCase
import com.ordolabs.domain.usecase.local.ValidateColorRgbBaseUseCase
import com.ordolabs.thecolor.mapper.toDomain
import com.ordolabs.thecolor.model.InputHexPresentation
import com.ordolabs.thecolor.model.InputRgbPresentation
import com.ordolabs.thecolor.util.ColorUtil.Color
import com.ordolabs.thecolor.util.ColorUtil.from
import com.ordolabs.thecolor.util.ColorUtil.toColorHex
import com.ordolabs.thecolor.util.ColorUtil.toColorRgb
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.shareOnceIn
import com.ordolabs.thecolor.util.ext.updateGuaranteed
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty
import com.ordolabs.thecolor.util.struct.getOrNull
import com.ordolabs.thecolor.util.struct.loading
import com.ordolabs.thecolor.util.struct.success
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect

class ColorInputViewModel(
    private val validateColorHexUseCase: ValidateColorHexBaseUseCase,
    private val validateColorRgbUseCase: ValidateColorRgbBaseUseCase
) : BaseViewModel() {

    private val _colorValidationState = MutableStateResourceFlow<Boolean>(Resource.empty())
    val colorValidationState = _colorValidationState.shareOnceIn(viewModelScope)

    private val _colorPreview: MutableStateFlow<Resource<Color>>
    val colorPreview: StateFlow<Resource<Color>>

    private val _inputHex: MutableStateFlow<Resource<InputHexPresentation>>
    val inputHex: SharedFlow<Resource<InputHexPresentation>>

    private val _inputRgb: MutableStateFlow<Resource<InputRgbPresentation>>
    val inputRgb: SharedFlow<Resource<InputRgbPresentation>>

    private val _procceedCommand = MutableStateResourceFlow<Color>(Resource.empty())
    val procceedCommand = _procceedCommand.shareOnceIn(viewModelScope)

    private var colorValidationJob: Job? = null

    init {
        _colorPreview = MutableStateResourceFlow(Color("000000"))
        colorPreview = _colorPreview.asStateFlow()

        _inputHex = MutableStateResourceFlow(Resource.loading())
        inputHex = _inputHex.shareOnceIn(viewModelScope)

        _inputRgb = MutableStateResourceFlow(Resource.loading())
        inputRgb = _inputRgb.shareOnceIn(viewModelScope)

        colorPreview.value.ifSuccess { color ->
            updateColorValidationState(valid = true)
            updateColors(color, Nothing::class.java)
        }
    }

    override fun onCleared() {
        super.onCleared()
        colorValidationJob?.cancel()
        colorValidationJob = null
    }

    fun validateColor(input: InputHexPresentation) = launch {
        restartColorValidation().join()
        _inputHex.value = Resource.success(input)
        val domain = input.toDomain() ?: kotlin.run {
            updateColorValidationState(valid = false)
            return@launch
        }
        val abstract = Color.from(input)
        colorValidationJob = launch {
            validateColorHexUseCase.invoke(domain).collect { valid ->
                onColorValidated(valid, abstract, input::class.java)
            }
        }
    }

    fun validateColor(input: InputRgbPresentation) = launch {
        restartColorValidation().join()
        _inputRgb.value = Resource.success(input)
        val domain = input.toDomain() ?: kotlin.run {
            updateColorValidationState(valid = false)
            return@launch
        }
        val abstract = Color.from(input)
        colorValidationJob = launch {
            validateColorRgbUseCase.invoke(domain).collect { valid ->
                onColorValidated(valid, abstract, input::class.java)
            }
        }
    }

    fun procceedInput() {
        val color = _colorPreview.value.getOrNull() ?: return
        _procceedCommand.updateGuaranteed {
            Resource.success(color)
        }
    }

    private fun restartColorValidation() = launchInMain {
        colorValidationJob?.cancel()
        _colorValidationState.value = Resource.loading()
    }

    private fun onColorValidated(
        valid: Boolean,
        abstract: Color?,
        initialColorClass: Class<*>
    ) {
        updateColorValidationState(valid)
        if (valid && abstract != null) {
            updateColors(abstract, initialColorClass)
            updateColorPreview(abstract)
        } else {
            clearColors()
            clearColorPreview()
        }
    }

    private fun updateColorValidationState(valid: Boolean) {
        _colorValidationState.value = Resource.success(valid)
    }

    private fun updateColors(color: Color, exclude: Class<*>) {
        if (exclude != InputHexPresentation::class.java) {
            _inputHex.value = Resource.success(color.toColorHex())
        }
        if (exclude != InputRgbPresentation::class.java) {
            _inputRgb.value = Resource.success(color.toColorRgb())
        }
    }

    private fun clearColors() {
        _inputHex.value = Resource.empty()
        _inputRgb.value = Resource.empty()
    }

    private fun updateColorPreview(color: Color) {
        _colorPreview.value = Resource.success(color)
    }

    private fun clearColorPreview() {
        _colorPreview.value = Resource.empty()
    }
}