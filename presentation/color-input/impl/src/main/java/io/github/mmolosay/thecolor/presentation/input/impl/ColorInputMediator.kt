package io.github.mmolosay.thecolor.presentation.input.impl

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.presentation.input.api.ColorInput
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputColorStore
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

/**
 * Acts as mediator between ViewModels of different color inputs.
 * The responsibility of this component is to synchronize data between different color inputs.
 * This class may also be used to set a specific color to all color inputs.
 *
 * Once one ViewModel [send]s a [Color] (presumably parsed from [ColorInput]),
 * all other color input ViewModels get the same color data through their specific flows.
 * This way if user was using one specific View, after switching to other View they will see
 * the UI with the same data (color) they have left on in previous View.
 *
 * Any update is also sent to [colorInputColorStore], which can be used to obtain current abstract
 * color.
 */
class ColorInputMediator @AssistedInject constructor(
    @Assisted private val colorInputColorStore: ColorInputColorStore,
    private val colorInputMapper: ColorInputMapper,
    private val colorConverter: ColorConverter,
    private val colorInputFactory: ColorInputFactory,
) {

    private val colorStateFlow = MutableSharedFlow<ColorState>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    private var lastSourceInputType: InputType? = null

    val hexColorInputFlow: Flow<ColorInput.Hex> =
        ColorInputFlow(
            inputType = InputType.Hex,
            emptyColorInput = colorInputFactory::emptyHex,
            colorToColorInput = {
                val typedColor = with(colorConverter) { it.toHex() }
                with(colorInputMapper) { typedColor.toColorInput() }
            }
        )

    val rgbColorInputFlow: Flow<ColorInput.Rgb> =
        ColorInputFlow(
            inputType = InputType.Rgb,
            emptyColorInput = colorInputFactory::emptyRgb,
            colorToColorInput = {
                val typedColor = with(colorConverter) { it.toRgb() }
                with(colorInputMapper) { typedColor.toColorInput() }
            }
        )

    private fun <T : ColorInput> ColorInputFlow(
        inputType: InputType,
        emptyColorInput: () -> T,
        colorToColorInput: (Color) -> T,
    ): Flow<T> =
        colorStateFlow
            .filter { lastSourceInputType != inputType } // prevent interrupting user
            .map { colorState ->
                when (colorState) {
                    is ColorState.AbsentOrInvalid -> emptyColorInput()
                    is ColorState.Valid -> colorToColorInput(colorState.color)
                }
            }

    init {
        // should be 100% successful with BufferOverflow.DROP_OLDEST
        colorStateFlow.tryEmit(ColorState.AbsentOrInvalid)
    }

    /**
     * Propagates specified [color] to color input flows (e.g. [hexColorInputFlow]).
     * Parameter [from] defines which color input flow will NOT receive an update to avoid
     * update loop.
     *
     * Passing `null` [color] will emit empty [ColorInput]s from flows.
     * Passing `null` [from] will not ignore any flow and all of them will emit.
     */
    suspend fun send(
        color: Color?,
        from: InputType?,
    ) {
        lastSourceInputType = from
        colorInputColorStore.set(color)
        colorStateFlow.emit(color.toState())
    }

    private fun Color?.toState(): ColorState =
        if (this != null) {
            ColorState.Valid(color = this)
        } else {
            ColorState.AbsentOrInvalid
        }

    /** State of the color the user is currently working with in color input View */
    private sealed interface ColorState {
        data object AbsentOrInvalid : ColorState
        data class Valid(val color: Color) : ColorState
    }

    /** Used to differentiate between color input Views. */
    enum class InputType {
        Hex, Rgb,
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            colorInputColorStore: ColorInputColorStore,
        ): ColorInputMediator
    }
}

