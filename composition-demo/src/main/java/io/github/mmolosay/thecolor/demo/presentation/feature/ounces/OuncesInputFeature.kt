package io.github.mmolosay.thecolor.demo.presentation.feature.ounces

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.composition.slice
import io.github.mmolosay.thecolor.demo.domain.DemoSettings
import io.github.mmolosay.thecolor.demo.presentation.feature.input.Submission
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldFeature
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputStateLenses as StateLenses

class OuncesInputFeature @AssistedInject constructor(
    @Assisted scope: CoroutineScope,
    @Assisted private val store: Store<OuncesInputState, OuncesInputOutcome>,
    @Assisted private val core: OuncesInputCore,
    textFieldFactory: TextFieldFeature.Factory,
    settings: DemoSettings, // from dependency injection: only the subscription uses it
) {

    val viewData: StateFlow<OuncesInputViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    // for its View, which shows the field's View
    val textField: TextFieldFeature = run {
        // nothing to decide: the field has no outcomes, its core keeps the rule for its text, and a
        // submission made on older text is no longer pending
        val slice = store.slice(StateLenses.textField) { state -> Change(state) }
        textFieldFactory.create(
            scope = scope.child(),
            store = slice,
            core = core.textFieldCore,
        )
    }

    init {
        // while its scope is active: the setting has one writer, so each value applies as it arrives
        scope.launch {
            settings.decimalSeparator.filterNotNull().collect { separator ->
                store.update { state -> with(core) { state.receiveSeparator(separator) } }
            }
        }
    }

    fun answerShown(submission: Submission) {
        store.update { state -> with(core) { state.answerShown(submission) } }
    }

    fun submit() {
        store.update { state -> with(core) { state.submit() } }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            scope: CoroutineScope,
            store: Store<OuncesInputState, OuncesInputOutcome>,
            core: OuncesInputCore,
        ): OuncesInputFeature
    }
}
