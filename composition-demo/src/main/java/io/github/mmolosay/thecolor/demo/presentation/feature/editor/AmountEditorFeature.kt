package io.github.mmolosay.thecolor.demo.presentation.feature.editor

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.composition.slice
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.CupDialFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.GramsInputFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.GramsInputOutcome
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputOutcome
import kotlinx.coroutines.CoroutineScope
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorStateLenses as StateLenses

// No view data of its own: its View only lays out the inputs' Views
class AmountEditorFeature @AssistedInject constructor(
    @Assisted core: AmountEditorCore,
    @Assisted store: Store<AmountEditorState, AmountEditorOutcome>,
    @Assisted scope: CoroutineScope,
    gramsFactory: GramsInputFeature.Factory,
    ouncesFactory: OuncesInputFeature.Factory,
    cupsFactory: CupDialFeature.Factory,
) {

    // for its View, which shows the inputs' Views
    val grams: GramsInputFeature = gramsFactory.create(
        core = core.gramsCore,
        store = store.slice(StateLenses.grams) { state, outcome: GramsInputOutcome? ->
            with(core) { state.onGrams(outcome) }
        },
        scope = scope.child(),
    )

    val ounces: OuncesInputFeature = ouncesFactory.create(
        core = core.ouncesCore,
        store = store.slice(StateLenses.ounces) { state, outcome: OuncesInputOutcome? ->
            with(core) { state.onOunces(outcome) }
        },
        scope = scope.child(),
    )

    val cups: CupDialFeature = cupsFactory.create(
        core = core.cupsCore,
        store = store.slice(StateLenses.cups) { state -> with(core) { state.onCups() } },
        scope = scope.child(),
    )

    @AssistedFactory
    fun interface Factory {
        fun create(
            core: AmountEditorCore,
            store: Store<AmountEditorState, AmountEditorOutcome>,
            scope: CoroutineScope,
        ): AmountEditorFeature
    }
}
