package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.ColorFactory
import io.github.mmolosay.thecolor.domain.usecase.GetInitialColorUseCase
import io.github.mmolosay.thecolor.presentation.ColorInputColorStore
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.isCompleteFromUserPerspective
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
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
    private val getInitialColor: GetInitialColorUseCase,
    private val colorInputToAbstract: ColorInputToAbstractColorUseCase,
    private val colorInputMapper: ColorInputMapper,
    private val colorConverter: ColorConverter,
    private val colorInputFactory: ColorInputFactory,
    private val colorInputColorStore: ColorInputColorStore,
) {

    val colorStateFlow = MutableSharedFlow<ColorState?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    private var lastUsedInputType: InputType? = null

    val hexColorInputFlow: Flow<ColorInput.Hex> =
        colorStateFlow
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
        colorStateFlow
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

    suspend fun init() {
        colorStateFlow.emit(getInitialColor().toState())
    }

    suspend fun send(input: ColorInput) {
        lastUsedInputType = input.type()
        val color = with(colorInputToAbstract) { input.toAbstractOrNull() }
        colorInputColorStore.updateWith(color)
        val state = color.toState()
        colorStateFlow.emit(state)
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
    sealed interface ColorState {
        data object Invalid : ColorState // unfinished color
        data class Valid(val color: Color.Abstract) : ColorState
    }

    /** Used to differentiate between color input Views. */
    private enum class InputType {
        Hex,
        Rgb,
    }
}

@Singleton
class ColorInputToAbstractColorUseCase @Inject constructor(
    private val colorInputMapper: ColorInputMapper,
    private val colorFactory: ColorFactory,
    private val colorConverter: ColorConverter,
) {

    fun ColorInput.toAbstractOrNull(): Color.Abstract? {
        if (!isCompleteFromUserPerspective()) return null
        val prototype = with(colorInputMapper) { toPrototype() }
        val color = colorFactory.from(prototype) ?: return null
        return with(colorConverter) { color.toAbstract() }
    }
}