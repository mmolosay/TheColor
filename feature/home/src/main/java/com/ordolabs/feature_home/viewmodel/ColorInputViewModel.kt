package com.ordolabs.feature_home.viewmodel

import androidx.lifecycle.viewModelScope
import com.ordolabs.domain.usecase.local.ValidateColorHexBaseUseCase
import com.ordolabs.domain.usecase.local.ValidateColorRgbBaseUseCase
import com.ordolabs.thecolor.mapper.toDomain
import com.ordolabs.thecolor.model.InputHexPresentation
import com.ordolabs.thecolor.model.InputRgbPresentation
import com.ordolabs.thecolor.util.ColorUtil.Color
import com.ordolabs.thecolor.util.ColorUtil.from
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.shareOnceIn
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty
import com.ordolabs.thecolor.util.struct.getOrNull
import com.ordolabs.thecolor.util.struct.success
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect

class ColorInputViewModel(
    private val validateColorHexUseCase: ValidateColorHexBaseUseCase,
    private val validateColorRgbUseCase: ValidateColorRgbBaseUseCase
) : BaseViewModel() {

    private val _colorPreview: MutableStateFlow<Resource<Color>>
    val colorPreview: StateFlow<Resource<Color>>

    private val _procceedCommand = MutableStateResourceFlow<Color>(Resource.empty())
    val procceedCommand = _procceedCommand.shareOnceIn(viewModelScope)

    private var colorValidationJob: Job? = null

    init {
        _colorPreview = MutableStateResourceFlow(Resource.empty())
        colorPreview = _colorPreview.asStateFlow()
    }

    override fun onCleared() {
        super.onCleared()
        colorValidationJob?.cancel()
        colorValidationJob = null
    }

    fun validateColor(input: InputHexPresentation) = launch {
        restartColorValidation().join()
        val domain = input.toDomain()
        colorValidationJob = launch {
            validateColorHexUseCase.invoke(domain).collect { valid ->
                onColorValidated(valid, Color.from(input))
            }
        }
    }

    fun validateColor(input: InputRgbPresentation) = launch {
        restartColorValidation().join()
        val domain = input.toDomain()
        colorValidationJob = launch {
            validateColorRgbUseCase.invoke(domain).collect { valid ->
                onColorValidated(valid, Color.from(input))
            }
        }
    }

    fun procceedInput() {
        val color = _colorPreview.value.getOrNull() ?: return
        _procceedCommand.value = Resource.success(color)
    }

    private fun restartColorValidation() = launchInMain {
        colorValidationJob?.cancel()
    }

    private fun onColorValidated(
        valid: Boolean,
        color: Color? = null
    ) {
        if (valid && color != null) {
            updateColorPreview(color)
        } else {
            clearColorPreview()
        }
    }

    private fun updateColorPreview(color: Color) {
        _colorPreview.value = Resource.success(color)
    }

    private fun clearColorPreview() {
        _colorPreview.value = Resource.empty()
    }
}