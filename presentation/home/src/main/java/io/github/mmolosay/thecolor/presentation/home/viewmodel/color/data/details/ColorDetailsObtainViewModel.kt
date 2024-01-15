package io.github.mmolosay.thecolor.presentation.home.viewmodel.color.data.details

import io.github.mmolosay.thecolor.domain.usecase.GetColorDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.mapper.toPresentation
import io.github.mmolosay.thecolor.presentation.color.Color
import io.github.mmolosay.thecolor.presentation.color.data.ColorDetails
import io.github.mmolosay.thecolor.utils.Resource
import io.github.mmolosay.thecolor.utils.empty
import io.github.mmolosay.thecolor.utils.failure
import io.github.mmolosay.thecolor.utils.loading
import io.github.mmolosay.thecolor.utils.success
import io.github.mmolosay.thecolor.presentation.viewmodel.BaseViewModel
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