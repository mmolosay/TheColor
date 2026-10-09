package io.github.mmolosay.thecolor.presentation.scheme.viewmodel

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorRepository
import io.github.mmolosay.thecolor.domain.color.ColorRepository.GetColorSchemeRequest
import io.github.mmolosay.thecolor.domain.color.ColorScheme.Mode
import io.github.mmolosay.thecolor.domain.color.IsColorLightUseCase
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.IoDispatcher
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeData.Swatch
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeData.SwatchCount
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeState.Request
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import javax.inject.Inject
import javax.inject.Singleton
import io.github.mmolosay.thecolor.domain.color.ColorScheme as DomainColorScheme

/**
 * Handles presentation logic of the 'Color Scheme' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
class ColorSchemeViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    private val colorRepository: ColorRepository,
    private val createData: CreateColorSchemeDataUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val _stateFlow = MutableStateFlow<ColorSchemeState>(ColorSchemeState.Idle)
    val stateFlow: StateFlow<ColorSchemeState> = _stateFlow.asStateFlow()

    private val dataEditor = ColorSchemeDataEditor()

    /**
     * Fetches [DomainColorScheme] for the specified "[seed]" color of the color scheme.
     * Exposes fetched color scheme from the [stateFlow].
     */
    suspend fun fetchColorScheme(seed: Color) {
        val request = stateFlow.value.request(seed)
        val domainRequest = request.toDomainRequest()
        _stateFlow.update {
            ColorSchemeState.Loading(request)
        }
        yield() // allow 'dataStateFlow' to emit 'Loading' state in unit tests // TODO: may not be needed anymore; debug unit tests and verify
        val schemeResult = withContext(ioDispatcher) {
            colorRepository.getColorScheme(domainRequest)
        }
        val colorScheme = schemeResult.getOrElse { exception ->
            _stateFlow.update { current ->
                if (!current.isAwaiting(request)) return@update current
                val error = ColorSchemeError(
                    cause = exception,
                )
                ColorSchemeState.Error(
                    request = request,
                    error = error,
                )
            }
            return
        }
        _stateFlow.update { current ->
            if (!current.isAwaiting(request)) return@update current
            val data = createData(scheme = colorScheme, request = request)
            ColorSchemeState.Ready(
                request = request,
                data = data,
                domainColorScheme = colorScheme,
            )
        }
    }

    fun selectMode(mode: Mode) {
        _stateFlow.update { state ->
            if (state !is ColorSchemeState.Ready) return@update state
            val newData = with(dataEditor) {
                state.data.copyConsistently(selectedMode = mode)
            }
            state.copy(data = newData)
        }
    }

    fun selectSwatchCount(count: SwatchCount) {
        _stateFlow.update { state ->
            if (state !is ColorSchemeState.Ready) return@update state
            val newData = with(dataEditor) {
                state.data.copyConsistently(selectedSwatchCount = count)
            }
            state.copy(data = newData)
        }
    }

    private fun Request.toDomainRequest(): GetColorSchemeRequest =
        GetColorSchemeRequest(
            seed = this.seed,
            mode = this.mode,
            swatchCount = this.swatchCount.value,
        )

    private fun ColorSchemeState.request(seed: Color): Request {
        val defaultMode = Mode.Monochrome
        val defaultSwatchCount = SwatchCount.Six
        return when (this) {
            is ColorSchemeState.Idle -> Request(
                seed = seed,
                mode = defaultMode,
                swatchCount = defaultSwatchCount,
            )
            is ColorSchemeState.Loading -> this.request.copy(seed = seed)
            is ColorSchemeState.Ready -> Request(
                seed = seed,
                mode = this.data.selectedMode,
                swatchCount = this.data.selectedSwatchCount,
            )
            is ColorSchemeState.Error -> this.request.copy(seed = seed)
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
        ): ColorSchemeViewModel
    }
}

@Singleton
// 'private' but Dagger
class CreateColorSchemeDataUseCase @Inject constructor(
    private val colorToColorInt: ColorToColorIntUseCase,
    private val isColorLight: IsColorLightUseCase,
) {

    operator fun invoke(
        scheme: DomainColorScheme,
        request: Request,
    ) =
        ColorSchemeData(
            swatches = scheme.swatchDetails
                .map { details -> details.color.toSwatch() }
                .toPersistentList(),
            activeMode = request.mode,
            selectedMode = request.mode,
            activeSwatchCount = request.swatchCount,
            selectedSwatchCount = request.swatchCount,
            hasChangesToApply = false, // 'active' and 'selected' values are same initially
        )

    private fun Color.toSwatch() =
        Swatch(
            color = with(colorToColorInt) { toColorInt() },
            isDark = with(isColorLight) { isLight().not() },
        )
}

/**
 * Creates updated copies of [ColorSchemeData] while ensuring data consistency.
 */
private class ColorSchemeDataEditor {

    fun ColorSchemeData.copyConsistently(
        selectedMode: Mode = this.selectedMode,
        selectedSwatchCount: SwatchCount = this.selectedSwatchCount,
    ): ColorSchemeData =
        this.copy(
            selectedMode = selectedMode,
            selectedSwatchCount = selectedSwatchCount,
            hasChangesToApply = hasChangesToApply(
                selectedMode,
                this.activeMode,
                selectedSwatchCount,
                this.activeSwatchCount,
            ),
        )

    private fun hasChangesToApply(
        selectedMode: Mode,
        activeMode: Mode,
        selectedSwatchCount: SwatchCount,
        activeSwatchCount: SwatchCount,
    ): Boolean {
        val hasModeChanged by lazy { selectedMode != activeMode }
        val hasSwatchCountChanged by lazy { selectedSwatchCount != activeSwatchCount }
        return (hasModeChanged || hasSwatchCountChanged)
    }
}