package io.github.mmolosay.thecolor.presentation.details.feature

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.CompositionUtils.serve
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.domain.color.ColorRepository
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class ColorDetailsFeature @AssistedInject constructor(
    @Assisted private val core: ColorDetailsCore,
    @Assisted private val store: Store<ColorDetailsState, ColorDetailsOutcome>,
    @Assisted private val scope: CoroutineScope,
    private val colorRepository: ColorRepository,
) {

    val viewData: StateFlow<ColorDetailsViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    init {
        serveDetails()
    }

    private fun serveDetails() {
        scope.serve(
            store = store,
            request = { state -> with(core) { state.awaited() } },
        ) { request ->
            val result = colorRepository.getColorDetails(request.color)
            store.update { state ->
                with(core) { state.completeDetails(result, request) }
            }
        }
    }

    fun select(role: ColorRole) =
        store.update { state ->
            with(core) { state.select(role) }
        }

    fun retry() =
        store.update { state ->
            with(core) { state.retry() }
        }

    @AssistedFactory
    fun interface Factory {
        fun create(
            core: ColorDetailsCore,
            store: Store<ColorDetailsState, ColorDetailsOutcome>,
            scope: CoroutineScope,
        ): ColorDetailsFeature
    }
}