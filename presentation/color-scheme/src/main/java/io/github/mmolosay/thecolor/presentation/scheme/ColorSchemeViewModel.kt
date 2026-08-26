package io.github.mmolosay.thecolor.presentation.scheme

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorRepository
import io.github.mmolosay.thecolor.domain.color.ColorRepository.GetColorSchemeRequest
import io.github.mmolosay.thecolor.domain.color.ColorScheme.Mode
import io.github.mmolosay.thecolor.domain.color.IsColorLightUseCase
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.IoDispatcher
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.Changes
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.Swatch
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.SwatchCount
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeState.Request
import io.github.mmolosay.thecolor.utils.CoroutineRegistry
import io.github.mmolosay.thecolor.utils.Store
import io.github.mmolosay.thecolor.utils.trackThisAsSingleActive
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
    @Assisted private val store: Store<ColorSchemeState>,
    @Assisted private val eventHandler: ColorSchemeEventHandler,
    private val colorRepository: ColorRepository,
    private val createData: CreateColorSchemeDataUseCase,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    val stateFlow: StateFlow<ColorSchemeState> = store.flow

    private val opRegistry = CoroutineRegistry<Operation>()
    private val dataEditor = ColorSchemeDataEditor(
        applyChanges = {
            coroutineScope.launch(defaultDispatcher) {
                applyChanges()
            }
        },
    )

    /**
     * Fetches [DomainColorScheme] for the specified "[seed]" color of the color scheme.
     * Exposes fetched color scheme from the [stateFlow].
     */
    fun fetchColorScheme(seed: Color): Job =
        coroutineScope.launch(defaultDispatcher) {
            opRegistry.trackThisAsSingleActive(
                predicate = { it.value is Operation.FetchColorScheme },
                value = Operation.FetchColorScheme(seed),
            ) {
                val request = assembleRequest(seed)
                val domainRequest = request.toDomainRequest()
                store.update {
                    ColorSchemeState.Loading(request)
                }
                yield() // allow 'dataStateFlow' to emit 'Loading' state in unit tests // TODO: may not be needed anymore; debug unit tests and verify
                val schemeResult = withContext(ioDispatcher) {
                    colorRepository.getColorScheme(domainRequest)
                }
                val colorScheme = schemeResult.getOrElse { exception ->
                    val tryAgain: () -> Unit = {
                        fetchColorScheme(seed)
                    }
                    val error = ColorSchemeError(
                        cause = exception,
                        tryAgain = tryAgain,
                    )
                    store.update {
                        ColorSchemeState.Error(
                            request = request,
                            error = error,
                        )
                    }
                    return@launch
                }
                val data = createData(scheme = colorScheme, request = request)
                store.update {
                    ColorSchemeState.Ready(
                        request = request,
                        data = data,
                        domainColorScheme = colorScheme,
                    )
                }
            }
        }

    private fun createData(
        scheme: DomainColorScheme,
        request: Request,
    ) =
        createData.invoke(
            scheme = scheme,
            request = request,
            onSwatchSelect = {
                coroutineScope.launch(defaultDispatcher) {
                    sendSwatchSelectedEvent(it)
                }
            },
            onModeSelect = {
                coroutineScope.launch(defaultDispatcher) {
                    selectMode(it)
                }
            },
            onSwatchCountSelect = {
                coroutineScope.launch(defaultDispatcher) {
                    selectSwatchCount(it)
                }
            },
        )

    private suspend fun sendSwatchSelectedEvent(indexOfSelectedSwatch: Int) {
        val state = store.current()
        if (state !is ColorSchemeState.Ready) return // stale invocation
        val swatch = state.data.swatches.getOrNull(indexOfSelectedSwatch) ?: return
        val swatchColorDetails =
            state.domainColorScheme.swatchDetails.getOrNull(indexOfSelectedSwatch) ?: return
        val event = ColorSchemeEvent.SwatchSelected(swatch, swatchColorDetails)
        eventHandler.offer(event)
    }

    private suspend fun selectMode(mode: Mode) {
        store.update { state ->
            if (state !is ColorSchemeState.Ready) return@update state
            val newData = with(dataEditor) {
                state.data.copyConsistently(selectedMode = mode)
            }
            state.copy(data = newData)
        }
    }

    private suspend fun selectSwatchCount(count: SwatchCount) {
        store.update { state ->
            if (state !is ColorSchemeState.Ready) return@update state
            val newData = with(dataEditor) {
                state.data.copyConsistently(selectedSwatchCount = count)
            }
            state.copy(data = newData)
        }
    }

    private suspend fun applyChanges() {
        val state = store.current()
        if (state !is ColorSchemeState.Ready) return // stale invocation
        if (state.data.changes !is Changes.Present) return // nothing to apply
        fetchColorScheme(seed = state.request.seed)
    }

    private fun ColorSchemeEventHandler.offer(event: ColorSchemeEvent) {
        if (!coroutineScope.isActive) return
        this.invoke(event)
    }

    private fun Request.toDomainRequest(): GetColorSchemeRequest =
        GetColorSchemeRequest(
            seed = this.seed,
            mode = this.mode,
            swatchCount = this.swatchCount.value,
        )

    private suspend fun assembleRequest(seed: Color): Request {
        val defaultMode = Mode.Monochrome
        val defaultSwatchCount = SwatchCount.Six
        return when (val state = store.current()) {
            is ColorSchemeState.Idle -> Request(
                seed = seed,
                mode = defaultMode,
                swatchCount = defaultSwatchCount,
            )
            is ColorSchemeState.Loading -> state.request.copy(seed = seed)
            is ColorSchemeState.Ready -> Request(
                seed = seed,
                mode = state.data.selectedMode,
                swatchCount = state.data.selectedSwatchCount,
            )
            is ColorSchemeState.Error -> state.request.copy(seed = seed)
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            store: Store<ColorSchemeState>,
            eventHandler: ColorSchemeEventHandler,
        ): ColorSchemeViewModel
    }

    /**
     * Directly maps to the public methods of the [ColorSchemeViewModel].
     * Implements "Command" design pattern.
     */
    private sealed interface Operation {

        /**
         * Corresponds to the [ColorSchemeViewModel.fetchColorScheme] method.
         */
        data class FetchColorScheme(
            val seed: Color,
        ) : Operation
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
        onSwatchSelect: (indexOfSwatch: Int) -> Unit,
        onModeSelect: (Mode) -> Unit,
        onSwatchCountSelect: (SwatchCount) -> Unit,
    ) =
        ColorSchemeData(
            swatches = scheme.swatchDetails.map { details ->
                details.color.toSwatch()
            },
            onSwatchSelect = onSwatchSelect,
            activeMode = request.mode,
            selectedMode = request.mode,
            onModeSelect = onModeSelect,
            activeSwatchCount = request.swatchCount,
            selectedSwatchCount = request.swatchCount,
            onSwatchCountSelect = onSwatchCountSelect,
            changes = Changes.None, // 'active' and 'selected' values are same initially
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
// 'private' but Dagger
class ColorSchemeDataEditor(
    private val applyChanges: () -> Unit,
) {

    fun ColorSchemeData.copyConsistently(
        selectedMode: Mode = this.selectedMode,
        selectedSwatchCount: SwatchCount = this.selectedSwatchCount,
    ): ColorSchemeData =
        this.copy(
            selectedMode = selectedMode,
            selectedSwatchCount = selectedSwatchCount,
            changes = Changes(
                selectedMode,
                this.activeMode,
                selectedSwatchCount,
                this.activeSwatchCount,
            ),
        )

    private fun Changes(
        selectedMode: Mode,
        activeMode: Mode,
        selectedSwatchCount: SwatchCount,
        activeSwatchCount: SwatchCount,
    ): Changes {
        val hasModeChanged by lazy { selectedMode != activeMode }
        val hasSwatchCountChanged by lazy { selectedSwatchCount != activeSwatchCount }
        val hasChanges = (hasModeChanged || hasSwatchCountChanged)
        return if (hasChanges) Changes.Present(applyChanges) else Changes.None
    }
}