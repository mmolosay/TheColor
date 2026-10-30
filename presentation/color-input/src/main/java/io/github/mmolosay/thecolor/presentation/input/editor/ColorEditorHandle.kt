package io.github.mmolosay.thecolor.presentation.input.editor

import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexFacade
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexHandle
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvFacade
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvHandle
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbFacade
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbHandle
import io.github.mmolosay.thecolor.utils.Atom
import kotlinx.coroutines.flow.StateFlow

// temporary: the old input ViewModels take an Atom; each of its updates is one update of the store
fun <S, O> Store<S, O>.asAtom(): Atom<S> =
    StoreAtom(store = this)

private class StoreAtom<S, O>(
    private val store: Store<S, O>,
) : Atom<S> {

    override val value: S
        get() = store.value

    override fun update(transform: (S) -> S) =
        store.update { state -> Change(transform(state)) }
}

interface ColorEditorHandle {
    val dataFlow: StateFlow<ColorEditorData>
    fun facade(data: ColorEditorData): ColorEditorFacade
}

data class ColorEditorFacade(
    val hex: ColorInputHexFacade,
    val rgb: ColorInputRgbFacade,
    val hsv: ColorInputHsvFacade,
)

fun ColorEditorHandle(
    dataFlow: StateFlow<ColorEditorData>,
    hex: ColorInputHexHandle,
    rgb: ColorInputRgbHandle,
    hsv: ColorInputHsvHandle,
): ColorEditorHandle =
    ColorEditorHandleImpl(
        dataFlow = dataFlow,
        hex = hex,
        rgb = rgb,
        hsv = hsv,
    )

private class ColorEditorHandleImpl(
    override val dataFlow: StateFlow<ColorEditorData>,
    private val hex: ColorInputHexHandle,
    private val rgb: ColorInputRgbHandle,
    private val hsv: ColorInputHsvHandle,
) : ColorEditorHandle {

    override fun facade(data: ColorEditorData): ColorEditorFacade =
        ColorEditorFacade(
            hex = hex.facade(data.hex),
            rgb = rgb.facade(data.rgb),
            hsv = hsv.facade(data.hsv),
        )
}