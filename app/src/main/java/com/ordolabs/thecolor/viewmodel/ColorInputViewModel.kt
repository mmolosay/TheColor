package com.ordolabs.thecolor.viewmodel

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
import com.ordolabs.thecolor.util.struct.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect

class ColorInputViewModel(
    private val validateColorHexUseCase: ValidateColorHexBaseUseCase,
    private val validateColorRgbUseCase: ValidateColorRgbBaseUseCase
) : BaseViewModel() {

    val colorValidationState: LiveData<Resource<Boolean>> get() = _colorValidationState
    private val _colorValidationState: MutableLiveData<Resource<Boolean>>

    val colorPreview: LiveData<Resource<Color>> get() = _colorPreview
    private val _colorPreview: MutableLiveData<Resource<Color>>

    val colorHex: LiveData<Resource<ColorHexPresentation>> get() = _colorHex
    private val _colorHex: MutableLiveData<Resource<ColorHexPresentation>>

    val colorRgb: LiveData<Resource<ColorRgbPresentation>> get() = _colorRgb
    private val _colorRgb: MutableLiveData<Resource<ColorRgbPresentation>>

    private var colorValidationJob: Job? = null

    init {
        _colorValidationState = MutableLiveData(Resource.success(false))
        _colorPreview = MutableLiveData(Resource.loading())
        _colorHex = MutableLiveData(Resource.loading())
        _colorRgb = MutableLiveData(Resource.loading())
    }

    override fun onCleared() {
        super.onCleared()
        colorValidationJob?.cancel()
    }

    fun validateColor(color: ColorHexPresentation?) {
        colorValidationJob?.cancel()
        _colorValidationState.value = Resource.loading()
        val colorDomain = color?.toDomain() ?: kotlin.run {
            _colorValidationState.value = Resource.success(false)
            return
        }
        val abstract = Color.from(color)
        updateColors(abstract, color::class.java)

        colorValidationJob = launchCoroutine {
            validateColorHexUseCase.invoke(colorDomain).collect { valid ->
                _colorValidationState.value = Resource.success(valid)
                updateColorPreview(abstract, valid)
            }
        }
    }

    fun validateColor(color: ColorRgbPresentation?) {
        colorValidationJob?.cancel()
        _colorValidationState.value = Resource.loading()
        val colorDomain = color?.toDomain() ?: kotlin.run {
            _colorValidationState.value = Resource.success(false)
            return
        }
        val abstract = Color.from(color)
        updateColors(abstract, color::class.java)

        colorValidationJob = launchCoroutine {
            validateColorRgbUseCase.invoke(colorDomain).collect { valid ->
                _colorValidationState.value = Resource.success(valid)
                updateColorPreview(abstract, valid)
            }
        }
    }

    private fun updateColors(color: Color, exclude: Class<*>) {
        if (exclude != ColorHexPresentation::class.java) {
            _colorHex.value = Resource.success(color.toColorHex())
        }
        if (exclude != ColorRgbPresentation::class.java) {
            _colorRgb.value = Resource.success(color.toColorRgb())
        }
    }

    private fun updateColorPreview(color: Color, valid: Boolean) {
        if (valid) _colorPreview.value = Resource.success(color)
    }
}