package io.github.mmolosay.thecolor.presentation.scheme.viewmodel

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
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeData.Swatch
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeData.SwatchCount
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeState.Request
import io.github.mmolosay.thecolor.utils.CoroutineRegistry
import io.github.mmolosay.thecolor.utils.Store
import io.github.mmolosay.thecolor.utils.trackThisAsSingleActive
import kotlinx.collections.immutable.toPersistentList
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

    private val exclusiveLane = defaultDispatcher.limitedParallelism(1)
    private val opRegistry = CoroutineRegistry<ColorSchemeAction>()
    private val dataEditor = ColorSchemeDataEditor()

    fun execute(action: ColorSchemeAction): Job =
        coroutineScope.launch(exclusiveLane) {
            when (action) {
                is ColorSchemeAction.OnSwatchSelect -> {
                    opRegistry.trackThisAsSingleActive(
                        predicate = { it.value is ColorSchemeAction.OnSwatchSelect },
                        value = action,
                    ) {
                        sendSwatchSelectedEvent(indexOfSelectedSwatch = action.index)
                    }
                }
                is ColorSchemeAction.SelectMode -> {
                    opRegistry.trackThisAsSingleActive(
                        predicate = { it.value is ColorSchemeAction.SelectMode },
                        value = action,
                    ) {
                        selectMode(mode = action.mode)
                    }
                }
                is ColorSchemeAction.SelectSwatchCount -> {
                    opRegistry.trackThisAsSingleActive(
                        predicate = { it.value is ColorSchemeAction.SelectSwatchCount },
                        value = action,
                    ) {
                        selectSwatchCount(count = action.count)
                    }
                }
                is ColorSchemeAction.ApplyChanges -> {
                    opRegistry.trackThisAsSingleActive(
                        predicate = { it.value is ColorSchemeAction.ApplyChanges },
                        value = action,
                    ) {
                        applyChanges()
                    }
                }
                is ColorSchemeAction.RetryOnError -> {
                    opRegistry.trackThisAsSingleActive(
                        predicate = { it.value is ColorSchemeAction.RetryOnError },
                        value = action,
                    ) {
                        retryOnError()
                    }
                }
            }
        }

    /**
     * Fetches [DomainColorScheme] for the specified "[seed]" color of the color scheme.
     * Exposes fetched color scheme from the [stateFlow].
     */
    // TODO: invocations of this method are not coordinated with each other.
    //  Fetches made by the outer caller (like parent ViewModel) and the ones from 'execute()' ('ApplyChanges', 'RetryOnError')
    //  belong to different mechanisms and can overlap: each writes 'Loading' and then its own result, so the state settles
    //  on whichever finishes last — possibly a scheme for a stale seed.
    //  Repro: apply changes, then select a color on the 'Color Details' page while the fetch is in flight.
    //  Tracking this method again is not an option: it would register the caller's job and let this ViewModel
    //  cancel its caller's operation. Fix by coordinating all invocations in the caller.
    suspend fun fetchColorScheme(seed: Color) {
        val request = store.current().request(seed)
        val domainRequest = request.toDomainRequest()
        store.update {
            ColorSchemeState.Loading(request)
        }
        yield() // allow 'dataStateFlow' to emit 'Loading' state in unit tests // TODO: may not be needed anymore; debug unit tests and verify
        val schemeResult = withContext(ioDispatcher) {
            colorRepository.getColorScheme(domainRequest)
        }
        val colorScheme = schemeResult.getOrElse { exception ->
            val error = ColorSchemeError(
                cause = exception,
            )
            store.update {
                ColorSchemeState.Error(
                    request = request,
                    error = error,
                )
            }
            return
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
        if (!state.data.hasChangesToApply) return // nothing to apply
        fetchColorScheme(seed = state.request.seed)
    }

    private suspend fun retryOnError() {
        val state = store.current()
        if (state !is ColorSchemeState.Error) return // stale invocation
        fetchColorScheme(seed = state.request.seed)
    }

    private suspend fun ColorSchemeEventHandler.offer(event: ColorSchemeEvent) {
        if (!coroutineScope.isActive) return
        this.invoke(event)
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
            store: Store<ColorSchemeState>,
            eventHandler: ColorSchemeEventHandler,
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