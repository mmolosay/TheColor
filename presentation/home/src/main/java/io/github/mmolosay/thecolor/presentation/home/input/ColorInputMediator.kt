package io.github.mmolosay.thecolor.presentation.home.input

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.ColorPrototypeConverter
import io.github.mmolosay.thecolor.presentation.color.ColorInput
import io.github.mmolosay.thecolor.presentation.color.isCompleteFromUserPerspective
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldViewModel.State
import io.github.mmolosay.thecolor.presentation.mapper.toColorInput
import io.github.mmolosay.thecolor.presentation.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Acts as mediator between ViewModels of different color input Views.
 * The responsibility of this component is to synchronize data between different color input Views.
 *
 * Once one View [send]s a [State], all other Views get the same state through flows.
 * This way if user was using one specific View, after switching to other View they will see
 * the UI with the same data (color) they have left on in previous View.
 */
@Singleton
class ColorInputMediator @Inject constructor(
    private val colorPrototypeConverter: ColorPrototypeConverter,
    private val colorConverter: ColorConverter,
) {

    private val stateFlow = MutableStateFlow<State<Color.Abstract>>(State.Empty)
    private var lastUsedInputType: InputType = InputType.Hex // TODO: I don't like that it is hardcoded

    val hexStateFlow: Flow<State<ColorInput.Hex>> =
        stateFlow.transform { state ->
            if (lastUsedInputType == InputType.Hex) return@transform // prevent user input interrupting
            state.mapType {
                with(colorConverter) { it.toHex() }.toColorInput()
            }.also {
                emit(it)
            }
        }

    val rgbStateFlow: Flow<State<ColorInput.Rgb>> =
        stateFlow.transform { state ->
            if (lastUsedInputType == InputType.Rgb) return@transform // prevent user input interrupting
            state.mapType {
                with(colorConverter) { it.toRgb() }.toColorInput()
            }.also {
                emit(it)
            }
        }

    fun <C : ColorInput> send(state: State<C>) {
        val result = runCatching {
            state.mapType { colorInput ->
                fun updateLastUsedInputType() {
                    lastUsedInputType = colorInput.toInputType()
                }
                if (!colorInput.isCompleteFromUserPerspective()) {
                    updateLastUsedInputType()
                    error("If color becomes incomplete, clear other color input Views")
                }
                val prototype = colorInput.toDomain()
                val color = with(colorPrototypeConverter) { prototype.toColorOrNull() }
                if (color == null) {
                    updateLastUsedInputType()
                    error("If color is not valid, clear other color input Views")
                }
                val abstract = with(colorConverter) { color.toAbstract() }
                updateLastUsedInputType()
                abstract
            }
        }
        stateFlow.value = result.getOrElse { State.Empty }
    }

    private inline fun <T, R> State<T>.mapType(
        transformation: (T) -> R,
    ): State<R> =
        when (this) {
            is State.Empty -> this
            is State.Populated -> {
                val newColor = transformation(color)
                State.Populated(newColor)
            }
        }

    private fun ColorInput.toInputType() =
        when (this) {
            is ColorInput.Hex -> InputType.Hex
            is ColorInput.Rgb -> InputType.Rgb
        }

    /** Used to differentiate between color input Views. */
    private enum class InputType {
        Hex,
        Rgb,
    }
}