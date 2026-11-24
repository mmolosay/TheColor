package io.github.mmolosay.thecolor.presentation.input.editor

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexFeature
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexOutcome
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvFeature
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbFeature
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbOutcome
import io.github.mmolosay.thecolor.utils.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.utils.composition.Store
import io.github.mmolosay.thecolor.utils.composition.slice
import kotlinx.coroutines.CoroutineScope
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorStateLenses as StateLenses

class ColorEditorFeature @AssistedInject constructor(
    @Assisted private val core: ColorEditorCore,
    @Assisted private val store: Store<ColorEditorState, ColorEditorOutcome>,
    @Assisted scope: CoroutineScope,
    hexFactory: ColorInputHexFeature.Factory,
    rgbFactory: ColorInputRgbFeature.Factory,
    hsvFactory: ColorInputHsvFeature.Factory,
) {

    val hex: ColorInputHexFeature = hexFactory.create(
        core = core.hexCore,
        store = store.slice(StateLenses.hex) { state, outcome: ColorInputHexOutcome? ->
            with(core) { state.onHex(outcome) }
        },
        scope = scope.child(),
    )
    val rgb: ColorInputRgbFeature = rgbFactory.create(
        core = core.rgbCore,
        store = store.slice(StateLenses.rgb) { state, outcome: ColorInputRgbOutcome? ->
            with(core) { state.onRgb(outcome) }
        },
        scope = scope.child(),
    )
    val hsv: ColorInputHsvFeature = hsvFactory.create(
        core = core.hsvCore,
        store = store.slice(StateLenses.hsv) { state ->
            with(core) { state.onHsv() }
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