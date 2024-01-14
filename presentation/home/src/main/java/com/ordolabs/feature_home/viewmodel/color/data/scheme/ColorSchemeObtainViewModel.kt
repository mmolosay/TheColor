package com.ordolabs.feature_home.viewmodel.color.data.scheme

import com.ordolabs.domain.usecase.GetColorSchemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.presentation.mapper.toDomain
import io.github.mmolosay.presentation.mapper.toPresentation
import io.github.mmolosay.presentation.model.color.data.ColorScheme
import io.github.mmolosay.presentation.model.color.data.ColorSchemeRequest
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
import com.ordolabs.domain.model.ColorSchemeRequest as ColorSchemeRequestDomain

@HiltViewModel
class ColorSchemeObtainViewModel @Inject constructor(
    private val getColorScheme: GetColorSchemeUseCase
) : BaseViewModel() {

    private val _scheme = MutableStateFlow<Resource<ColorScheme>>(Resource.empty())
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
            try {
                val scheme = getColorScheme(request).toPresentation()
                _scheme.value = Resource.success(scheme)
            } catch (e: Throwable) {
                _scheme.update { it.failure(e) }
            }
        }

    private fun restartGettingColorScheme() {
        getColorSchemeJob?.cancel()
        _scheme.value = Resource.loading()
    }
}