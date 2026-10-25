package io.github.mmolosay.thecolor.presentation.input.editor

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.presentation.input.ColorInputMapper
import io.github.mmolosay.thecolor.presentation.input.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexState
import io.github.mmolosay.thecolor.presentation.input.hex.color
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvState
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbState
import io.github.mmolosay.thecolor.presentation.input.rgb.color
import io.github.mmolosay.thecolor.utils.Atom
import io.github.mmolosay.thecolor.utils.Lens
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorStateLenses as Lenses

class ColorEditor @AssistedInject constructor(
    @Assisted initialState: ColorEditorState,
    private val colorConverter: ColorConverter,
    private val colorInputMapper: ColorInputMapper,
    private val colorInputValidator: ColorInputValidator,
) {

    private val stateFlow = MutableStateFlow(initialState)
    val dataFlow: StateFlow<ColorEditorData> = stateFlow.mapState { it.toData() }
    val colorStateFlow: StateFlow<ColorState> = stateFlow.mapState { it.colorState }

    val hexAtom: Atom<ColorInputHexState> = InputStateAtom(
        type = DomainColorInputType.Hex,
        lens = Lenses.hex,
        getColor = { context(colorInputValidator) { it.color() } },
    )
    val rgbAtom: Atom<ColorInputRgbState> = InputStateAtom(
        type = DomainColorInputType.Rgb,
        lens = Lenses.rgb,
        getColor = { context(colorInputValidator) { it.color() } },
    )
    val hsvAtom: Atom<ColorInputHsvState> = InputStateAtom(
        type = DomainColorInputType.Hsv,
        lens = Lenses.hsv,
        getColor = ColorInputHsvState::color,
    )

    fun setColor(color: Color?) {
        context(colorConverter, colorInputMapper) {
            stateFlow.update {
                it.withColor(color, source = null)
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            initialState: ColorEditorState,
        ): ColorEditor
    }

    private inner class InputStateAtom<T>(
        private val type: DomainColorInputType,
        private val lens: Lens<ColorEditorState, T>,
        private val getColor: (T) -> Color?,
    ) : Atom<T> {

        override val value: T
            get() = lens.get(stateFlow.value)

        override fun update(transform: (T) -> T) {
            stateFlow.update { current ->
                val old = lens.get(current)
                val new = transform(old)
                val newColor = getColor(new)
                val base = when (newColor) {
                    getColor(old) -> current // this input still stands for the same color
                    else -> context(colorConverter, colorInputMapper) {
                        current.withColor(newColor, type)
                    }
                }
                lens.set(base, new)
            }
        }
    }
}

val ColorEditor.colorState: ColorState
    get() = this.colorStateFlow.value