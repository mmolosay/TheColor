package io.github.mmolosay.thecolor.input

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.ColorPrototypeConverter
import io.github.mmolosay.thecolor.presentation.color.ColorInput
import io.github.mmolosay.thecolor.presentation.color.isCompleteFromUserPerspective
import io.github.mmolosay.thecolor.presentation.mapper.toColorInput
import io.github.mmolosay.thecolor.presentation.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.transform
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Acts as mediator between ViewModels of different color input Views.
 * The responsibility of this component is to synchronize data between different color input Views.
 *
 * Once one View [send]s a [ColorInput], all other Views get the same color data through flows.
 * This way if user was using one specific View, after switching to other View they will see
 * the UI with the same data (color) they have left on in previous View.
 */
@Singleton
class ColorInputMediator @Inject constructor(
    private val colorPrototypeConverter: ColorPrototypeConverter,
    private val colorConverter: ColorConverter,
) {

    private val stateFlow = MutableStateFlow<ColorState?>(null)
    private var lastUsedInputType: InputType =
        InputType.Hex // TODO: I don't like that it is hardcoded

    val hexColorInputFlow: Flow<ColorInput.Hex> =
        stateFlow
            .filterNotNull()
            .transform { state ->
                if (lastUsedInputType == InputType.Hex) return@transform // prevent user input interrupting
                val input = when (state) {
                    is ColorState.Invalid -> ColorInput.Hex(string = "")
                    is ColorState.Valid -> with(colorConverter) { state.color.toHex() }.toColorInput()
                }
                emit(input)
            }

    val rgbColorInputFlow: Flow<ColorInput.Rgb> =
        stateFlow
            .filterNotNull()
            .transform { state ->
                if (lastUsedInputType == InputType.Rgb) return@transform // prevent user input interrupting
                val input = when (state) {
                    is ColorState.Invalid -> ColorInput.Rgb(r = "", g = "", b = "")
                    is ColorState.Valid -> with(colorConverter) { state.color.toRgb() }.toColorInput()
                }
                emit(input)
            }

    fun send(input: ColorInput) {
        lastUsedInputType = input.type()
        val result = runCatching {
            if (!input.isCompleteFromUserPerspective())
                error("Color in not complete from user perspective yet, thus invalid")
            val prototype = input.toDomain()
            val color = with(colorPrototypeConverter) { prototype.toColorOrNull() }
                ?: error("Color is invalid")
            with(colorConverter) { color.toAbstract() }
        }
        val color = result.getOrNull()
        val state = if (color != null) {
            ColorState.Valid(color)
        } else {
            ColorState.Invalid
        }
        stateFlow.value = state
    }

    private fun ColorInput.type() =
        when (this) {
            is ColorInput.Hex -> InputType.Hex
            is ColorInput.Rgb -> InputType.Rgb
        }

    /** State of the color the user is currently working with in color input View */
    private sealed interface ColorState {
        data object Invalid : ColorState // unfinished color
        data class Valid(val color: Color.Abstract) : ColorState
    }

    /** Used to differentiate between color input Views. */
    private enum class InputType {
        Hex,
        Rgb,
    }
}