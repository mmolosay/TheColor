package io.github.mmolosay.thecolor.input

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.ColorFactory
import io.github.mmolosay.thecolor.domain.usecase.GetInitialColorUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
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
    private val colorFactory: ColorFactory,
    private val colorConverter: ColorConverter,
    private val colorInputFactory: ColorInputFactory,
) {

    private val stateFlow = MutableStateFlow<ColorState?>(null)
    private var lastUsedInputType: InputType? = null

    init {
        // TODO: use real coroutine scope
        runBlocking {
            stateFlow.value = getInitialColor().toState()
        }
    }

    val hexColorInputFlow: Flow<ColorInput.Hex> =
        stateFlow
            .filterNotNull()
            .filter { lastUsedInputType != InputType.Hex } // prevent interrupting user
            .map {
                when (it) {
                    is ColorState.Invalid -> colorInputFactory.emptyHex()
                    is ColorState.Valid -> {
                        val domainColor = with(colorConverter) { it.color.toHex() }
                        with(colorInputMapper) { domainColor.toColorInput() }
                    }
                }
            }

    val rgbColorInputFlow: Flow<ColorInput.Rgb> =
        stateFlow
            .filterNotNull()
            .filter { lastUsedInputType != InputType.Rgb } // prevent interrupting user
            .map {
                when (it) {
                    is ColorState.Invalid -> colorInputFactory.emptyRgb()
                    is ColorState.Valid -> {
                        val domainColor = with(colorConverter) { it.color.toRgb() }
                        with(colorInputMapper) { domainColor.toColorInput() }
                    }
                }
            }

    fun send(input: ColorInput) {
        lastUsedInputType = input.type()
        stateFlow.value = input.toAbstractOrNull().toState()
    }

    private fun ColorInput.toAbstractOrNull(): Color.Abstract? {
        if (!isCompleteFromUserPerspective()) return null
        val prototype = with(colorInputMapper) { toPrototype() }
        val color = colorFactory.from(prototype) ?: return null
        return with(colorConverter) { color.toAbstract() }
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