package io.github.mmolosay.thecolor.presentation.scheme.viewmodel

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.CompositionUtils.serve
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.domain.color.ColorRepository
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.IoDispatcher
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import io.github.mmolosay.thecolor.domain.color.ColorScheme as DomainColorScheme


class ColorSchemeFeature @AssistedInject constructor(
    @Assisted private val core: ColorSchemeCore,
    @Assisted private val store: Store<ColorSchemeState, ColorSchemeOutcome>,
    @Assisted private val scope: CoroutineScope,
    private val colorRepository: ColorRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    val viewData: StateFlow<ColorSchemeViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    init {
        serveScheme()
    }

    private fun serveScheme() {
        scope.serve(
            store = store,
            request = { state -> with(core) { state.awaited() } },
        ) { request ->
            val domainRequest = ColorRepository.GetColorSchemeRequest(
                seed = request.seed,
                mode = request.mode,
                swatchCount = request.swatchCount.value,
            )
            val result = withContext(ioDispatcher) {
                colorRepository.getColorScheme(domainRequest)
            }
            store.update { state ->
                with(core) { state.completeScheme(result, request) }
            }
        }
    }

    fun selectSwatch(index: Int) =
        store.update { state ->
            with(core) { state.selectSwatch(index) }
        }

    fun selectMode(mode: DomainColorScheme.Mode) =
        store.update { state ->
            with(core) { state.selectMode(mode) }
        }

    fun selectSwatchCount(count: SwatchCount) =
        store.update { state ->
            with(core) { state.selectSwatchCount(count) }
        }

    fun applyChanges() =
        store.update { state ->
            with(core) { state.applyChanges() }
        }

    fun retry() =
        store.update { state ->
            with(core) { state.retry() }
        }

    @AssistedFactory
    fun interface Factory {
        fun create(
            core: ColorSchemeCore,
            store: Store<ColorSchemeState, ColorSchemeOutcome>,
            scope: CoroutineScope,
        ): ColorSchemeFeature
    }
}