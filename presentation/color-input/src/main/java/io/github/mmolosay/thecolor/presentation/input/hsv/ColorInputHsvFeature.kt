package io.github.mmolosay.thecolor.presentation.input.hsv

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.CompositionUtils.serve
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration.Companion.milliseconds

class ColorInputHsvFeature @AssistedInject constructor(
    @Assisted private val core: ColorInputHsvCore,
    @Assisted private val store: Store<ColorInputHsvState, *>,
    @Assisted private val scope: CoroutineScope,
) {

    val viewData: StateFlow<ColorInputHsvViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    init {
        servePromotion()
    }

    private fun servePromotion() {
        scope.serve(
            store = store,
            request = { state -> with(core) { state.awaitedPromotion() } },
        ) { promotion ->
            delay(200.milliseconds)
            store.update { state ->
                with(core) { state.completePromotion(promotion) }
            }
        }
    }

    fun pick(
        newColor: (current: Color.Hsv) -> Color.Hsv,
    ) {
        store.update { state ->
            with(core) { state.pick(newColor) }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            core: ColorInputHsvCore,
            store: Store<ColorInputHsvState, *>,
            scope: CoroutineScope,
        ): ColorInputHsvFeature
    }
}