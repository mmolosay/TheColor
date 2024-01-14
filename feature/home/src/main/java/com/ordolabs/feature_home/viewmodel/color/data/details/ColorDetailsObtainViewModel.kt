package com.ordolabs.feature_home.viewmodel.color.data.details

import com.ordolabs.domain.usecase.remote.GetColorDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.presentation.mapper.toPresentation
import io.github.mmolosay.presentation.model.color.Color
import io.github.mmolosay.presentation.model.color.data.ColorDetails
import io.github.mmolosay.presentation.util.MutableStateResourceFlow
import io.github.mmolosay.presentation.util.ext.catchFailureIn
import io.github.mmolosay.presentation.util.ext.setLoading
import io.github.mmolosay.presentation.util.ext.setSuccess
import io.github.mmolosay.presentation.util.struct.Resource
import io.github.mmolosay.presentation.util.struct.empty
import io.github.mmolosay.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asStateFlow
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