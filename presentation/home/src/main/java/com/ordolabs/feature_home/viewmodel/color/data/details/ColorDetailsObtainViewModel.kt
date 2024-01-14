package com.ordolabs.feature_home.viewmodel.color.data.details

import com.ordolabs.domain.usecase.GetColorDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.presentation.mapper.toPresentation
import io.github.mmolosay.presentation.model.color.Color
import io.github.mmolosay.presentation.model.color.data.ColorDetails
import com.ordolabs.util.Resource
import com.ordolabs.util.empty
import com.ordolabs.util.failure
import com.ordolabs.util.loading
import com.ordolabs.util.success
import io.github.mmolosay.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ColorDetailsObtainViewModel @Inject constructor(
    private val getColorDetails: GetColorDetailsUseCase
) : BaseViewModel() {

    private val _details = MutableStateFlow<Resource<ColorDetails>>(Resource.empty())
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
        _details.value = Resource.success(details)
    }

    private fun performGetColorDetails(colorHex: String) =
        launchInIO {
            try {
                val details = getColorDetails(colorHex).toPresentation()
                _details.value = Resource.success(details)
            } catch (e: Throwable) {
                _details.update { it.failure(e) }
            }
        }

    private fun restartGettingColorDetails() {
        getColorDetailsJob?.cancel()
        _details.value = Resource.loading()
    }
}