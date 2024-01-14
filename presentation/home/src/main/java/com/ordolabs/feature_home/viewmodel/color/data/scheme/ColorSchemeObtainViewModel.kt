package com.ordolabs.feature_home.viewmodel.color.data.scheme

import com.ordolabs.domain.usecase.GetColorSchemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.presentation.mapper.toDomainOrNull
import io.github.mmolosay.presentation.mapper.toPresentation
import io.github.mmolosay.presentation.model.color.data.ColorScheme
import io.github.mmolosay.presentation.model.color.data.ColorSchemeRequest
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
import com.ordolabs.domain.model.ColorSchemeRequest as ColorSchemeRequestDomain

@HiltViewModel
class ColorSchemeObtainViewModel @Inject constructor(
    private val getColorSchemeUseCase: GetColorSchemeUseCase
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
        val domain = request.toDomainOrNull()
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