package io.github.mmolosay.thecolor.input

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.ColorPrototypeConverter
import io.github.mmolosay.thecolor.domain.usecase.GetInitialColorUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
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
    getInitialColor: GetInitialColorUseCase,
    private val colorInputMapper: ColorInputMapper,
    private val colorPrototypeConverter: ColorPrototypeConverter,
    private val colorConverter: ColorConverter,
) {

    private val stateFlow =
        MutableStateFlow<ColorState>(ColorState.Invalid) // TODO: make nullable or introduce Loading state
    private var lastUsedInputType: InputType? = null

    init {
        // TODO: use real coroutine scope
        runBlocking {
            stateFlow.value = getInitialColor().toState()
        }
    }

    val hexColorInputFlow: Flow<ColorInput.Hex> =
        stateFlow
            .filter { lastUsedInputType != InputType.Hex } // prevent interrupting user
            .map {
                when (it) {
                    is ColorState.Invalid -> ColorInput.Hex(string = "")
                    is ColorState.Valid -> {
                        val domainColor = with(colorConverter) { it.color.toHex() }
                        with(colorInputMapper) { domainColor.toColorInput() }
                    }
                }
            }

    val rgbColorInputFlow: Flow<ColorInput.Rgb> =
        stateFlow
            .filter { lastUsedInputType != InputType.Rgb } // prevent interrupting user
            .map {
                when (it) {
                    is ColorState.Invalid -> ColorInput.Rgb(r = "", g = "", b = "")
                    is ColorState.Valid -> {
                        val domainColor = with(colorConverter) { it.color.toRgb() }
                        with(colorInputMapper) { domainColor.toColorInput() }
                    }
                }
            }

    fun send(input: ColorInput) {
        lastUsedInputType = input.type()
        val result = runCatching {
            if (!input.isCompleteFromUserPerspective())
                error("Color in not complete from user perspective yet, thus invalid")
            val prototype = with(colorInputMapper) { input.toPrototype() }
            val color = with(colorPrototypeConverter) { prototype.toColorOrNull() }
                ?: error("Color is invalid")
            with(colorConverter) { color.toAbstract() }
        }
        stateFlow.value = result.getOrNull().toState()
    }

    private fun Color.Abstract?.toState(): ColorState =
        if (this != null) {
            ColorState.Valid(color = this)
        } else {
            ColorState.Invalid
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