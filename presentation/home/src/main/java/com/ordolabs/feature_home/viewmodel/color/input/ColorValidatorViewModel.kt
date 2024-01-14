package com.ordolabs.feature_home.viewmodel.color.input

import com.ordolabs.domain.usecase.ValidateColorHexUseCase
import com.ordolabs.domain.usecase.ValidateColorRgbUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.presentation.mapper.toDomainOrNull
import io.github.mmolosay.presentation.model.color.Color
import io.github.mmolosay.presentation.model.color.ColorPreview
import io.github.mmolosay.presentation.model.color.ColorPrototype
import io.github.mmolosay.presentation.model.color.from
import com.ordolabs.util.Resource
import com.ordolabs.util.empty
import com.ordolabs.util.getOrNull
import com.ordolabs.util.success
import io.github.mmolosay.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ColorValidatorViewModel @Inject constructor(
    private val validateHexColor: ValidateColorHexUseCase,
    private val validateRgbColor: ValidateColorRgbUseCase
) : BaseViewModel() {

    private val _colorPreview: MutableStateFlow<Resource<ColorPreview>>
    val colorPreview: StateFlow<Resource<ColorPreview>>

    private var colorValidationJob: Job? = null

    init {
        _colorPreview = MutableStateFlow(Resource.empty())
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
        val colorDomain = input.toDomainOrNull()
        this.colorValidationJob = launch {
            val valid = validateHexColor(colorDomain)
            val color = Color.from(input)
            onColorValidated(color, valid)
        }
    }

    private fun validateColor(input: ColorPrototype.Rgb) {
        restartColorValidation()
        val colorDomain = input.toDomainOrNull()
        this.colorValidationJob = launch {
            val valid = validateRgbColor(colorDomain)
            val color = Color.from(input)
            onColorValidated(color, valid)
        }
    }

    fun updateColorPreview(new: ColorPreview) {
        _colorPreview.value = Resource.success(new)
    }

    fun isSameAsColorPreview(prototype: ColorPrototype): Boolean {
        val color = Color.from(prototype) ?: return false
        val preview = colorPreview.value.getOrNull() ?: return false
        return (color == preview)
    }

    private fun clearColorPreview() {
        _colorPreview.value = Resource.empty()
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