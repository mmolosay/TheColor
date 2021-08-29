package com.ordolabs.feature_home.viewmodel

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ordolabs.domain.usecase.local.ValidateColorHexBaseUseCase
import com.ordolabs.domain.usecase.local.ValidateColorRgbBaseUseCase
import com.ordolabs.thecolor.mapper.toDomain
import com.ordolabs.thecolor.model.ColorHexPresentation
import com.ordolabs.thecolor.model.ColorRgbPresentation
import com.ordolabs.thecolor.util.ColorUtil.Color
import com.ordolabs.thecolor.util.ColorUtil.from
import com.ordolabs.thecolor.util.ColorUtil.toColorHex
import com.ordolabs.thecolor.util.ColorUtil.toColorRgb
import com.ordolabs.thecolor.util.ext.postSuccess
import com.ordolabs.thecolor.util.ext.setLoading
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect

class ColorInputViewModel(
    private val validateColorHexUseCase: ValidateColorHexBaseUseCase,
    private val validateColorRgbUseCase: ValidateColorRgbBaseUseCase
) : BaseViewModel() {

    private val colorValidationState: MutableLiveData<Resource<Boolean>> by lazy {
        MutableLiveData(Resource.success(false))
    }
    private val colorPreview: MutableLiveData<Resource<Color>> by lazy {
        MutableLiveData(Resource.loading())
    }
    private val colorHex: MutableLiveData<Resource<ColorHexPresentation>> by lazy {
        MutableLiveData(Resource.loading())
    }
    private val colorRgb: MutableLiveData<Resource<ColorRgbPresentation>> by lazy {
        MutableLiveData(Resource.loading())
    }
    private val procceedCommand: MutableLiveData<Color> by lazy {
        MutableLiveData()
    }

    private var colorValidationJob: Job? = null

    override fun onCleared() {
        super.onCleared()
        colorValidationJob?.cancel()
    }

    fun getColorValidationState(): LiveData<Resource<Boolean>> = colorValidationState
    fun getColorPreview(): LiveData<Resource<Color>> = colorPreview
    fun getColorHex(): LiveData<Resource<ColorHexPresentation>> = colorHex
    fun getColorRgb(): LiveData<Resource<ColorRgbPresentation>> = colorRgb
    fun getProcceedCommand(): LiveData<Color> = procceedCommand

    fun validateColor(color: ColorHexPresentation?) {
        resetColorValidation()
        val colorDomain = color?.toDomain() ?: kotlin.run {
            colorValidationState.value = Resource.success(false)
            return
        }
        val abstract = Color.from(color)

        colorValidationJob = launch {
            validateColorHexUseCase.invoke(colorDomain).collect { valid ->
                onColorValidated(valid, abstract, color::class.java)
            }
        }
    }

    fun validateColor(color: ColorRgbPresentation?) {
        resetColorValidation()
        val colorDomain = color?.toDomain() ?: kotlin.run {
            colorValidationState.value = Resource.success(false)
            return
        }
        val abstract = Color.from(color)

        colorValidationJob = launch {
            validateColorRgbUseCase.invoke(colorDomain).collect { valid ->
                onColorValidated(valid, abstract, color::class.java)
            }
        }
    }

    @MainThread
    fun procceedInput() {
        val color = colorPreview.value?.ifSuccess { it }
        procceedCommand.value = color
    }

    @MainThread
    private fun resetColorValidation() {
        colorValidationJob?.cancel()
        colorValidationState.setLoading()
    }

    private fun onColorValidated(valid: Boolean, abstract: Color, initialColorClass: Class<*>) {
        colorValidationState.postSuccess(valid)
        updateColors(abstract, initialColorClass)
        updateColorPreview(valid, abstract)
    }

    private fun updateColors(color: Color, exclude: Class<*>) {
        if (exclude != ColorHexPresentation::class.java) {
            colorHex.postSuccess(color.toColorHex())
        }
        if (exclude != ColorRgbPresentation::class.java) {
            colorRgb.postSuccess(color.toColorRgb())
        }
    }

    private fun updateColorPreview(valid: Boolean, color: Color) {
        if (valid) colorPreview.postSuccess(color)
    }
}