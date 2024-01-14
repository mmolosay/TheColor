package com.ordolabs.feature_home.viewmodel.color.data.details

import com.ordolabs.domain.usecase.remote.GetColorDetailsUseCase
import com.ordolabs.thecolor.mapper.toPresentation
import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.model.color.data.ColorDetails
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.catchFailureIn
import com.ordolabs.thecolor.util.ext.setLoading
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@HiltViewModel
class ColorDetailsObtainViewModel @Inject constructor(
    private val getColorDetailsUseCase: GetColorDetailsUseCase
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

    /**
     * Sets specified [details] as current ones to be collected by `View`.
     * Call this when you receive details from outside the 'View'.
     */
    fun setColorDetails(details: ColorDetails) {
        _details.setSuccess(details)
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