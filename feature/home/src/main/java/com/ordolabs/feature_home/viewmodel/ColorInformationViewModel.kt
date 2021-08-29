package com.ordolabs.feature_home.viewmodel

import com.ordolabs.domain.usecase.remote.GetColorInformationBaseUseCase
import com.ordolabs.thecolor.mapper.toPresentation
import com.ordolabs.thecolor.model.ColorInformationPresentation
import com.ordolabs.thecolor.util.ColorUtil.Color
import com.ordolabs.thecolor.util.MutableSharedResourceFlow
import com.ordolabs.thecolor.util.ext.emitIn
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.success
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asSharedFlow

class ColorInformationViewModel(
    private val getColorInformationUseCase: GetColorInformationBaseUseCase
) : BaseViewModel() {

    private val _information = MutableSharedResourceFlow<ColorInformationPresentation>()
    val information = _information.asSharedFlow()

    private var fetchColorInformationJob: Job? = null

    override fun onCleared() {
        super.onCleared()
        fetchColorInformationJob?.cancel()
    }

    fun fetchColorInformation(color: Color) {
        fetchColorInformationJob?.cancel()
        fetchColorInformationJob = launch {
            val hexString = color.hexWithNumberSign
            getColorInformationUseCase.invoke(hexString).emitIn(_information) { colorInformation ->
                Resource.success(colorInformation.toPresentation())
            }
        }
    }
}