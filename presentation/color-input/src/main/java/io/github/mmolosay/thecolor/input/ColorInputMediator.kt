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

    private val abstractColorFlow = MutableStateFlow<Color.Abstract?>(null)
    private var lastUsedInputType: InputType =
        InputType.Hex // TODO: I don't like that it is hardcoded

    val hexColorInputFlow: Flow<ColorInput.Hex> =
        abstractColorFlow.transform { abstract ->
            if (lastUsedInputType == InputType.Hex) return@transform // prevent user input interrupting
            if (abstract == null) {
                val empty = ColorInput.Hex(string = "")
                emit(empty)
                return@transform
            }
            val input = with(colorConverter) { abstract.toHex() }.toColorInput()
            emit(input)
        }

    val rgbColorInputFlow: Flow<ColorInput.Rgb> =
        abstractColorFlow.transform { abstract ->
            if (lastUsedInputType == InputType.Rgb) return@transform // prevent user input interrupting
            if (abstract == null) {
                val empty = ColorInput.Rgb(r = "", g = "", b = "")
                emit(empty)
                return@transform
            }
            val input = with(colorConverter) { abstract.toRgb() }.toColorInput()
            emit(input)
        }

    fun send(input: ColorInput) {
        fun clearStateFlowValue() {
            abstractColorFlow.value = null
        }

        fun updateLastUsedInputType() {
            lastUsedInputType = input.type()
        }

        if (!input.isCompleteFromUserPerspective()) {
            clearStateFlowValue()
            updateLastUsedInputType()
            return // If color becomes incomplete, clear other color input Views
        }
        val prototype = input.toDomain()
        val color = with(colorPrototypeConverter) { prototype.toColorOrNull() }
        if (color == null) {
            clearStateFlowValue()
            updateLastUsedInputType()
            return // If color is not valid, clear other color input Views
        }
        val abstract = with(colorConverter) { color.toAbstract() }
        updateLastUsedInputType()
        abstractColorFlow.value = abstract
    }

    private fun ColorInput.type() =
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