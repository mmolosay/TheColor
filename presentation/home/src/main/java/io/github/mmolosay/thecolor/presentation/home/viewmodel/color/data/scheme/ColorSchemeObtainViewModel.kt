package io.github.mmolosay.thecolor.presentation.home.viewmodel.color.data.scheme

import io.github.mmolosay.thecolor.domain.usecase.GetColorSchemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.mapper.toDomain
import io.github.mmolosay.thecolor.presentation.mapper.toPresentation
import io.github.mmolosay.thecolor.presentation.color.data.ColorScheme
import io.github.mmolosay.thecolor.presentation.color.data.ColorSchemeRequest
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
import io.github.mmolosay.thecolor.domain.model.ColorSchemeRequest as ColorSchemeRequestDomain

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