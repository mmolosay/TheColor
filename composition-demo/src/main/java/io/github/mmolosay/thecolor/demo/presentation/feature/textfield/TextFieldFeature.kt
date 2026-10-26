package io.github.mmolosay.thecolor.demo.presentation.feature.textfield

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class TextFieldFeature @AssistedInject constructor(
    @Assisted scope: CoroutineScope, // no work runs in it, but its parents needn't know that
    @Assisted private val store: Store<TextFieldState, *>,
    @Assisted private val core: TextFieldCore,
) {

    val viewData: StateFlow<TextFieldViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    fun clearText() {
        store.update { state -> with(core) { state.clearText() } }
    }

    fun enterText(text: String, expected: String) {
        store.update { state -> with(core) { state.enterText(text, expected) } }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            scope: CoroutineScope,
            store: Store<TextFieldState, *>,
            core: TextFieldCore,
        ): TextFieldFeature
    }
}
