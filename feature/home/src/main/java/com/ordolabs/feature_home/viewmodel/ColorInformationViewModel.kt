package com.ordolabs.feature_home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ordolabs.domain.usecase.remote.GetColorInformationBaseUseCase
import com.ordolabs.thecolor.mapper.toPresentation
import com.ordolabs.thecolor.model.ColorInformationPresentation
import com.ordolabs.thecolor.util.ColorUtil.Color
import com.ordolabs.thecolor.util.ext.postSuccess
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect

class ColorInformationViewModel(
    private val getColorInformationUseCase: GetColorInformationBaseUseCase
) : BaseViewModel() {

    private val colorInformation: MutableLiveData<Resource<ColorInformationPresentation>> by lazy {
        MutableLiveData()
    }

    private var fetchColorInformationJob: Job? = null

    override fun onCleared() {
        super.onCleared()
        fetchColorInformationJob?.cancel()
    }

    fun getColorInformation(): LiveData<Resource<ColorInformationPresentation>> = colorInformation

    fun fetchColorInformation(color: Color) {
        fetchColorInformationJob?.cancel()
        fetchColorInformationJob = launch {
            getColorInformationUseCase.invoke(color.hexWithNumberSign).collect { information ->
                colorInformation.postSuccess(information.toPresentation())
            }
        }
    }
}