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

    val colorHex: LiveData<Resource<ColorHexPresentation>> get() = _colorHex
    private val _colorHex: MutableLiveData<Resource<ColorHexPresentation>>

    val colorRgb: LiveData<Resource<ColorRgbPresentation>> get() = _colorRgb
    private val _colorRgb: MutableLiveData<Resource<ColorRgbPresentation>>

    private var colorValidationJob: Job? = null

    init {
        _colorValidationState = MutableLiveData(Resource.success(false))
        _colorHex = MutableLiveData(Resource.loading())
        _colorRgb = MutableLiveData(Resource.loading())
    }

    override fun onCleared() {
        super.onCleared()
        colorValidationJob?.cancel()
    }

    fun validateColor(color: ColorHexPresentation?) {
        colorValidationJob?.cancel()
        val colorDomain = color?.toDomain()
        if (colorDomain == null) {
            _colorValidationState.value = Resource.success(false)
            return
        }
        updateColors(Color.from(color), color::class.java)
        _colorValidationState.value = Resource.loading()
        colorValidationJob = launchCoroutine {
            validateColorHexUseCase.invoke(colorDomain).collect { valid ->
                _colorValidationState.value = Resource.success(valid)
            }
        }
    }

    fun validateColor(color: ColorRgbPresentation?) {
        colorValidationJob?.cancel()
        val colorDomain = color?.toDomain()
        if (colorDomain == null) {
            _colorValidationState.value = Resource.success(false)
            return
        }
        updateColors(Color.from(color), color::class.java)
        _colorValidationState.value = Resource.loading()
        colorValidationJob = launchCoroutine {
            validateColorRgbUseCase.invoke(colorDomain).collect { valid ->
                _colorValidationState.value = Resource.success(valid)
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
}