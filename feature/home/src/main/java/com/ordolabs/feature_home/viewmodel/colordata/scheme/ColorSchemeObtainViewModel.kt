package com.ordolabs.feature_home.viewmodel.colordata.scheme

import com.ordolabs.domain.usecase.remote.GetColorSchemeBaseUseCase
import com.ordolabs.thecolor.mapper.toPresentation
import com.ordolabs.thecolor.model.color.ColorPresentation
import com.ordolabs.thecolor.model.colordata.ColorScheme
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

class ColorSchemeObtainViewModel(
    private val getColorSchemeUseCase: GetColorSchemeBaseUseCase
) : BaseViewModel() {

    private val _scheme =
        MutableStateResourceFlow<ColorScheme>(Resource.empty())
    val scheme = _scheme.asStateFlow()

    private var getColorSchemeJob: Job? = null

    override fun onCleared() {
        super.onCleared()
        getColorSchemeJob?.cancel()
    }

    fun getColorScheme(seed: ColorPresentation) {
        restartGettingColorScheme()
        this.getColorSchemeJob = performGetColorScheme(seed.hex)
    }

    private fun performGetColorScheme(seedHex: String) =
        launchInIO {
            getColorSchemeUseCase.invoke(seedHex)
                .catchFailureIn(_scheme)
                .collect { schemeDomain ->
                    val scheme = schemeDomain.toPresentation()
                    _scheme.setSuccess(scheme)
                }
        }

    private fun restartGettingColorScheme() {
        getColorSchemeJob?.cancel()
        _scheme.setLoading()
    }
}