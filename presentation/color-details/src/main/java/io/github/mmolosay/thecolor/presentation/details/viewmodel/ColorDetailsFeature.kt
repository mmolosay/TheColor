package io.github.mmolosay.thecolor.presentation.details.viewmodel

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

class ColorDetailsFeature @AssistedInject constructor(
    @Assisted private val core: ColorDetailsCore,
    @Assisted private val store: Store<ColorDetailsState, ColorDetailsOutcome>,
    @Assisted private val scope: CoroutineScope,
    private val colorRepository: ColorRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    val viewData: StateFlow<ColorDetailsViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    init {
        serveDetails()
    }

    private fun serveDetails() {
        scope.serve(
            store = store,
            request = { state -> with(core) { state.awaited() } }, // TODO: should "was requested, not yet started" and "started, executing (loading)" statuses be separated for more clarity? Probably yes.
        ) { request ->
            // TODO: withContext should be moved to the Repository, as it's done for other Repositories in Data layer
            val result = withContext(ioDispatcher) {
                colorRepository.getColorDetails(request.color)
            }
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