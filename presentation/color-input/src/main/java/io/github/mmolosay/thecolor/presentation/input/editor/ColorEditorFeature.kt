package io.github.mmolosay.thecolor.presentation.input.editor

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexFeature
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexOutcome
import kotlinx.coroutines.CoroutineScope
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorStateLenses as StateLenses

class ColorEditorFeature @AssistedInject constructor(
    @Assisted private val core: ColorEditorCore,
    @Assisted private val store: Store<ColorEditorState, ColorEditorOutcome>,
    @Assisted scope: CoroutineScope,
    hexFactory: ColorInputHexFeature.Factory,
) {

    val hex: ColorInputHexFeature = hexFactory.create(
        core = core.hexCore,
        store = store.slice(StateLenses.hex) { state, outcome: ColorInputHexOutcome? ->
            with(core) { state.onHex(outcome) }
        },
        scope = scope.child(),
    )

    @AssistedFactory
    fun interface Factory {
        fun create(
            core: ColorEditorCore,
            store: Store<ColorEditorState, ColorEditorOutcome>,
            scope: CoroutineScope,
        ): ColorEditorFeature
    }
}