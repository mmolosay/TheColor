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
import com.ordolabs.thecolor.util.MutableSharedResourceFlow
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.loading
import com.ordolabs.thecolor.util.struct.success
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn

class ColorInputViewModel(
    private val validateColorHexUseCase: ValidateColorHexBaseUseCase,
    private val validateColorRgbUseCase: ValidateColorRgbBaseUseCase
) : BaseViewModel() {

    private val _colorValidationState = MutableSharedResourceFlow<Boolean>()
    val colorValidationState = _colorValidationState.asSharedFlow()

    private val _colorPreview = MutableSharedResourceFlow<Color>()
    val colorPreview = _colorPreview.asSharedFlow()

    private val _colorHex = MutableSharedResourceFlow<ColorHexPresentation>()
    val colorHex = _colorHex.asSharedFlow()

    private val _colorRgb = MutableSharedResourceFlow<ColorRgbPresentation>()
    val colorRgb = _colorRgb.asSharedFlow()

    private val _procceedCommand = MutableStateResourceFlow<Color>(Resource.loading())
    val procceedCommand = _procceedCommand.asStateFlow()

    private var colorValidationJob: Job? = null

    init {
        launch {
            _colorValidationState.emit(Resource.success(false))
            _colorPreview.emit(Resource.loading())
            _colorHex.emit(Resource.loading())
            _colorRgb.emit(Resource.loading())
        }
    }

    override fun onCleared() {
        super.onCleared()
        colorValidationJob?.cancel()
        colorValidationJob = null
    }

    fun validateColor(color: ColorHexPresentation?) = launch {
        resetColorValidation().join()
        val domain = color?.toDomain() ?: kotlin.run {
            _colorValidationState.emit(Resource.success(false))
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
            _colorValidationState.emit(Resource.success(false))
            return@launch
        }
        val abstract = Color.from(color)

        colorValidationJob = launch {
            validateColorRgbUseCase.invoke(domain).collect { valid ->
                onColorValidated(valid, abstract, color::class.java)
            }
        }
    }

    fun procceedInput() = launch {
        val resource = colorPreview.stateIn(viewModelScope).value
        val color = resource.ifSuccess { it } ?: return@launch
        _procceedCommand.emit(Resource.success(color))
    }

    private suspend fun resetColorValidation() = launch {
        colorValidationJob?.cancel()
        _colorValidationState.emit(Resource.loading())
    }

    private fun onColorValidated(
        result: Boolean,
        abstract: Color,
        initialColorClass: Class<*>
    ) =
        launch {
            _colorValidationState.emit(Resource.success(result))
            updateColors(abstract, initialColorClass)
            updateColorPreview(result, abstract)
        }

    private suspend fun updateColors(color: Color, exclude: Class<*>) {
        if (exclude != ColorHexPresentation::class.java) {
            _colorHex.emit(Resource.success(color.toColorHex()))
        }
        if (exclude != ColorRgbPresentation::class.java) {
            _colorRgb.emit(Resource.success(color.toColorRgb()))
        }
    }

    private suspend fun updateColorPreview(valid: Boolean, color: Color) {
        if (valid) _colorPreview.emit(Resource.success(color))
    }
}