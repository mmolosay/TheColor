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
    @Assisted scope: CoroutineScope,
    @Assisted store: Store<AmountEditorState, AmountEditorOutcome>,
    @Assisted core: AmountEditorCore,
    gramsFactory: GramsInputFeature.Factory,
    ouncesFactory: OuncesInputFeature.Factory,
    cupsFactory: CupDialFeature.Factory,
) {

    // for its View, which shows the inputs' Views
    val grams: GramsInputFeature = gramsFactory.create(
        scope = scope.child(),
        store = store.slice(StateLenses.grams) { state, outcome: GramsInputOutcome? ->
            with(core) { state.onGrams(outcome) }
        },
        core = core.gramsCore,
    )

    val ounces: OuncesInputFeature = ouncesFactory.create(
        scope = scope.child(),
        store = store.slice(StateLenses.ounces) { state, outcome: OuncesInputOutcome? ->
            with(core) { state.onOunces(outcome) }
        },
        core = core.ouncesCore,
    )

    val cups: CupDialFeature = cupsFactory.create(
        scope = scope.child(),
        store = store.slice(StateLenses.cups) { state -> with(core) { state.onCups() } },
        core = core.cupsCore,
    )

    @AssistedFactory
    fun interface Factory {
        fun create(
            scope: CoroutineScope,
            store: Store<AmountEditorState, AmountEditorOutcome>,
            core: AmountEditorCore,
        ): AmountEditorFeature
    }
}
