package com.ordolabs.feature_home.viewmodel

import androidx.lifecycle.viewModelScope
import com.ordolabs.domain.usecase.local.ValidateColorHexBaseUseCase
import com.ordolabs.domain.usecase.local.ValidateColorRgbBaseUseCase
import com.ordolabs.thecolor.mapper.toDomain
import com.ordolabs.thecolor.model.ColorHexPresentation
import com.ordolabs.thecolor.model.ColorRgbPresentation
import com.ordolabs.thecolor.util.ColorUtil.Color
import com.ordolabs.thecolor.util.ColorUtil.from
import com.ordolabs.thecolor.util.ColorUtil.toColorHex
import com.ordolabs.thecolor.util.ColorUtil.toColorRgb
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.shareOnceIn
import com.ordolabs.thecolor.util.ext.updateGuaranteed
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.loading
import com.ordolabs.thecolor.util.struct.success
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect

class ColorInputViewModel(
    private val validateColorHexUseCase: ValidateColorHexBaseUseCase,
    private val validateColorRgbUseCase: ValidateColorRgbBaseUseCase
) : BaseViewModel() {

    private val _colorValidationState = MutableStateResourceFlow(Resource.success(false))
    val colorValidationState = _colorValidationState.asStateFlow()

    private val _colorPreview = MutableStateResourceFlow<Color>(Resource.loading())
    val colorPreview = _colorPreview.asStateFlow()

    private val _colorHex = MutableStateResourceFlow<ColorHexPresentation>(Resource.loading())
    val colorHex = _colorHex.asStateFlow()

    private val _colorRgb = MutableStateResourceFlow<ColorRgbPresentation>(Resource.loading())
    val colorRgb = _colorRgb.asStateFlow()

    private val _procceedCommand = MutableStateResourceFlow<Color>(Resource.loading())
    val procceedCommand = _procceedCommand.shareOnceIn(viewModelScope)

    private var colorValidationJob: Job? = null

    override fun onCleared() {
        super.onCleared()
        colorValidationJob?.cancel()
        colorValidationJob = null
    }

    fun validateColor(color: ColorHexPresentation?) = launch {
        resetColorValidation().join()
        val domain = color?.toDomain() ?: kotlin.run {
            _colorValidationState.value = Resource.success(false)
            return@launch
        }
        val abstract = Color.from(color)

        colorValidationJob = launch {
            validateColorHexUseCase.invoke(domain).collect { valid ->
                onColorValidated(valid, abstract, color::class.java)
            }
        }
    }

    fun validateColor(color: ColorRgbPresentation?) = launch {
        resetColorValidation().join()
        val domain = color?.toDomain() ?: kotlin.run {
            _colorValidationState.value = Resource.loading()
            return@launch
        }
        val abstract = Color.from(color)

        colorValidationJob = launch {
            validateColorRgbUseCase.invoke(domain).collect { valid ->
                onColorValidated(valid, abstract, color::class.java)
            }
        }
    }

    fun procceedInput() {
        val color = colorPreview.value.getOrNull() ?: return
        _procceedCommand.updateGuaranteed {
            Resource.success(color)
        }
    }

    private fun resetColorValidation() = launch {
        colorValidationJob?.cancel()
        _colorValidationState.value = Resource.loading()
    }

    private fun onColorValidated(
        valid: Boolean,
        abstract: Color,
        initialColorClass: Class<*>
    ) =
        launch {
            _colorValidationState.value = Resource.success(valid)
            updateColors(abstract, initialColorClass)
            updateColorPreview(valid, abstract)
        }

    private fun updateColors(color: Color, exclude: Class<*>) {
        if (exclude != ColorHexPresentation::class.java) {
            _colorHex.value = Resource.success(color.toColorHex())
        }
        if (exclude != ColorRgbPresentation::class.java) {
            _colorRgb.value = Resource.success(color.toColorRgb())
        }
    }

    private fun updateColorPreview(valid: Boolean, color: Color) {
        if (valid) _colorPreview.value = Resource.success(color)
    }
}