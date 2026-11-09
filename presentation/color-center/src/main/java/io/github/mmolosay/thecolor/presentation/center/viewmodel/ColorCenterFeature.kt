package io.github.mmolosay.thecolor.presentation.center.viewmodel

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsFeature
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsOutcome
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeFeature
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeOutcome
import kotlinx.coroutines.CoroutineScope
import io.github.mmolosay.thecolor.presentation.center.viewmodel.ColorCenterStateLenses as StateLenses

class ColorCenterFeature @AssistedInject constructor(
    @Assisted private val core: ColorCenterCore,
    @Assisted private val store: Store<ColorCenterState, ColorCenterOutcome>,
    @Assisted scope: CoroutineScope,
    detailsFactory: ColorDetailsFeature.Factory,
    schemeFactory: ColorSchemeFeature.Factory,
) {

    val details: ColorDetailsFeature = detailsFactory.create(
        core = core.detailsCore,
        store = store.slice(StateLenses.details) { state, outcome: ColorDetailsOutcome? ->
            with(core) { state.onDetails(outcome) }
        },
        scope = scope.child(),
    )

    val scheme: ColorSchemeFeature = schemeFactory.create(
        core = core.schemeCore,
        store = store.slice(StateLenses.scheme) { state, outcome: ColorSchemeOutcome? ->
            with(core) { state.onScheme(outcome) }
        },
        scope = scope.child(),
    )

    @AssistedFactory
    fun interface Factory {
        fun create(
            core: ColorCenterCore,
            store: Store<ColorCenterState, ColorCenterOutcome>,
            scope: CoroutineScope,
        ): ColorCenterFeature
    }
}
