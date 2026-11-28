package io.github.mmolosay.thecolor.presentation.center.feature

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.details.feature.ColorDetailsFeature
import io.github.mmolosay.thecolor.presentation.details.feature.ColorDetailsOutcome
import io.github.mmolosay.thecolor.presentation.scheme.feature.ColorSchemeFeature
import io.github.mmolosay.thecolor.presentation.scheme.feature.ColorSchemeOutcome
import io.github.mmolosay.thecolor.utils.composition.Store
import kotlinx.coroutines.CoroutineScope
import io.github.mmolosay.thecolor.presentation.center.feature.ColorCenterStateLenses as StateLenses

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
        scope = scope,
    )

    val scheme: ColorSchemeFeature = schemeFactory.create(
        core = core.schemeCore,
        store = store.slice(StateLenses.scheme) { state, outcome: ColorSchemeOutcome? ->
            with(core) { state.onScheme(outcome) }
        },
        scope = scope,
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
