package com.ordolabs.feature_home.viewmodel

import com.ordolabs.domain.usecase.remote.GetColorInformationBaseUseCase
import com.ordolabs.thecolor.mapper.toPresentation
import com.ordolabs.thecolor.model.ColorInformationPresentation
import com.ordolabs.thecolor.util.ColorUtil.Color
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.loading
import com.ordolabs.thecolor.util.struct.success
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect

class ColorInformationViewModel(
    private val getColorInformationUseCase: GetColorInformationBaseUseCase
) : BaseViewModel() {

    private val _information =
        MutableStateResourceFlow<ColorInformationPresentation>(Resource.loading())
    val information = _information.asStateFlow()

    private var fetchColorInformationJob: Job? = null

    override fun onCleared() {
        super.onCleared()
        fetchColorInformationJob?.cancel()
    }

    fun fetchColorInformation(color: Color) {
        fetchColorInformationJob?.cancel()
        fetchColorInformationJob = launch {
            val hexString = color.hexWithNumberSign
            getColorInformationUseCase.invoke(hexString).collect { colorInfo ->
                val info = colorInfo.toPresentation()
                _information.value = Resource.success(info)
            }
        }
    }
}