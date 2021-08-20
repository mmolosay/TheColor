package com.ordolabs.thecolor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.michaelbull.result.Ok
import com.ordolabs.domain.usecase.local.ValidateColorHexBaseUseCase
import com.ordolabs.thecolor.mapper.toDomain
import com.ordolabs.thecolor.model.ColorHexPresentation
import com.ordolabs.thecolor.model.ColorRgbPresentation
import com.ordolabs.thecolor.util.struct.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect

class ColorInputViewModel(
    private val validateColorHexUseCase: ValidateColorHexBaseUseCase
) : BaseViewModel() {

    val colorValidationState: LiveData<Resource<Boolean>> get() = _colorValidationState
    private val _colorValidationState: MutableLiveData<Resource<Boolean>>

    private var colorValidatorJob: Job? = null

    init {
        _colorValidationState = MutableLiveData(Resource.success(false))
    }

    override fun onCleared() {
        super.onCleared()
        colorValidatorJob?.cancel()
    }

    fun validateColor(color: ColorHexPresentation?) {
        val colorDomainResult = color?.toDomain()
        if (colorDomainResult == null || colorDomainResult !is Ok) {
            _colorValidationState.value = Resource.success(false)
            return
        }
        _colorValidationState.value = Resource.loading()
        colorValidatorJob = launchCoroutine {
            validateColorHexUseCase.invoke(colorDomainResult.value).collect { valid ->
                _colorValidationState.value = Resource.success(valid)
            }
        }
    }

    fun validateColor(color: ColorRgbPresentation) {

    }
}