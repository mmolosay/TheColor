package com.ordolabs.feature_home.viewmodel.colordata.scheme

import com.ordolabs.domain.usecase.remote.GetColorSchemeBaseUseCase
import com.ordolabs.thecolor.mapper.toDomain
import com.ordolabs.thecolor.mapper.toPresentation
import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.model.color.data.ColorSchemeRequest
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
import javax.inject.Inject
import com.ordolabs.domain.model.ColorSchemeRequest as ColorSchemeRequestDomain

class ColorSchemeObtainViewModel @Inject constructor(
    private val getColorSchemeUseCase: GetColorSchemeBaseUseCase
) : BaseViewModel() {

    private val _scheme = MutableStateResourceFlow<ColorScheme>(Resource.empty())
    val scheme = _scheme.asStateFlow()

    private var getColorSchemeJob: Job? = null

    override fun onCleared() {
        super.onCleared()
        getColorSchemeJob?.cancel()
    }

    fun getColorScheme(request: ColorSchemeRequest) {
        restartGettingColorScheme()
        val domain = request.toDomain()
        this.getColorSchemeJob = performGetColorScheme(domain)
    }

    private fun performGetColorScheme(request: ColorSchemeRequestDomain) =
        launchInIO {
            getColorSchemeUseCase.invoke(request)
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