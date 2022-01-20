package com.ordolabs.feature_home.viewmodel.colorinput

import com.ordolabs.domain.usecase.local.ValidateColorHexBaseUseCase
import com.ordolabs.domain.usecase.local.ValidateColorRgbBaseUseCase
import com.ordolabs.thecolor.mapper.toDomain
import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.model.color.ColorPreview
import com.ordolabs.thecolor.model.color.ColorPrototype
import com.ordolabs.thecolor.model.color.from
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.setEmpty
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty
import com.ordolabs.thecolor.util.struct.getOrNull
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect

class ColorValidatorViewModel(
    private val validateColorHexUseCase: ValidateColorHexBaseUseCase,
    private val validateColorRgbUseCase: ValidateColorRgbBaseUseCase
) : BaseViewModel() {

    private val _colorPreview: MutableStateFlow<Resource<ColorPreview>>
    val colorPreview: StateFlow<Resource<ColorPreview>>

    private var colorValidationJob: Job? = null

    init {
        _colorPreview = MutableStateResourceFlow(Resource.empty())
        colorPreview = _colorPreview.asStateFlow()
    }

    override fun onCleared() {
        super.onCleared()
        colorValidationJob?.cancel()
    }

    fun validateColor(input: ColorPrototype) =
        when (input) {
            is ColorPrototype.Hex -> validateColor(input)
            is ColorPrototype.Rgb -> validateColor(input)
        }

    private fun validateColor(input: ColorPrototype.Hex) {
        restartColorValidation()
        val domain = input.toDomain()
        this.colorValidationJob = launch {
            validateColorHexUseCase.invoke(domain).collect { valid ->
                val color = Color.from(input)
                onColorValidated(color, valid)
            }
        }
    }

    private fun validateColor(input: ColorPrototype.Rgb) {
        restartColorValidation()
        val domain = input.toDomain()
        this.colorValidationJob = launch {
            validateColorRgbUseCase.invoke(domain).collect { valid ->
                val color = Color.from(input)
                onColorValidated(color, valid)
            }
        }
    }

    fun updateColorPreview(new: ColorPreview) {
        _colorPreview.setSuccess(new)
    }

    fun isSameAsColorPreview(prototype: ColorPrototype): Boolean {
        val color = Color.from(prototype) ?: return false
        val preview = colorPreview.value.getOrNull() ?: return false
        return (color == preview)
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