package com.ordolabs.feature_home.viewmodel.colorinput

import androidx.lifecycle.viewModelScope
import com.ordolabs.domain.usecase.local.ValidateColorHexBaseUseCase
import com.ordolabs.domain.usecase.local.ValidateColorRgbBaseUseCase
import com.ordolabs.thecolor.mapper.toDomain
import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.model.color.ColorHex
import com.ordolabs.thecolor.model.color.ColorPreview
import com.ordolabs.thecolor.model.color.ColorRgb
import com.ordolabs.thecolor.model.color.from
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.setEmpty
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.util.ext.shareOnceIn
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty
import com.ordolabs.thecolor.util.struct.getOrNull
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect

class ColorInputViewModel(
    // TODO: color validation should be performed in separate ViewModel
    private val validateColorHexUseCase: ValidateColorHexBaseUseCase,
    private val validateColorRgbUseCase: ValidateColorRgbBaseUseCase
) : BaseViewModel() {

    private val _colorPreview: MutableStateFlow<Resource<ColorPreview>>
    val colorPreview: StateFlow<Resource<ColorPreview>>

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
    }

    fun validateColor(input: ColorHex) {
        restartColorValidation()
        val domain = input.toDomain()
        this.colorValidationJob = launch {
            validateColorHexUseCase.invoke(domain).collect { valid ->
                val color = Color.from(input)
                onColorValidated(color, valid)
            }
        }
    }

    fun validateColor(input: ColorRgb) {
        restartColorValidation()
        val domain = input.toDomain()
        this.colorValidationJob = launch {
            validateColorRgbUseCase.invoke(domain).collect { valid ->
                val color = Color.from(input)
                onColorValidated(color, valid)
            }
        }
    }

    fun procceedInput() {
        val preview = _colorPreview.value.getOrNull() ?: return
        _procceedCommand.setSuccess(preview.color)
    }

    fun updateColorPreview(new: ColorPreview) {
        _colorPreview.setSuccess(new)
    }

    private fun clearColorPreview() {
        _colorPreview.setEmpty()
    }

    private fun restartColorValidation() {
        colorValidationJob?.cancel()
    }

    private fun onColorValidated(
        color: Color? = null,
        valid: Boolean
    ) {
        if (valid && color != null) {
            val new = ColorPreview(color, isUserInput = true)
            updateColorPreview(new)
        } else {
            clearColorPreview()
        }
    }
}