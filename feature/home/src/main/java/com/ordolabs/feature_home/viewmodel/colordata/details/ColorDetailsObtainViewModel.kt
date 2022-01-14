package com.ordolabs.feature_home.viewmodel.colordata.details

import com.ordolabs.domain.usecase.remote.GetColorDetailsBaseUseCase
import com.ordolabs.thecolor.mapper.toPresentation
import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.model.colordata.ColorDetails
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.catchFailureIn
import com.ordolabs.thecolor.util.ext.setLoading
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect

class ColorDetailsObtainViewModel(
    private val getColorDetailsUseCase: GetColorDetailsBaseUseCase
) : BaseViewModel() {

    private val _details = MutableStateResourceFlow<ColorDetails>(Resource.empty())
    val details = _details.asStateFlow()

    private var getColorDetailsJob: Job? = null

    override fun onCleared() {
        super.onCleared()
        getColorDetailsJob?.cancel()
    }

    fun getColorDetails(color: Color) {
        restartGettingColorDetails()
        this.getColorDetailsJob = performGetColorDetails(color.hex)
    }

    private fun performGetColorDetails(colorHex: String) =
        launchInIO {
            getColorDetailsUseCase.invoke(colorHex)
                .catchFailureIn(_details)
                .collect { detailsDomain ->
                    val details = detailsDomain.toPresentation()
                    _details.setSuccess(details)
                }
        }

    private fun restartGettingColorDetails() {
        getColorDetailsJob?.cancel()
        _details.setLoading()
    }
}