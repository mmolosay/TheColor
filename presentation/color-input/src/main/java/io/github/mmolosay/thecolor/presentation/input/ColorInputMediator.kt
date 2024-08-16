package io.github.mmolosay.thecolor.presentation.input

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.GetInitialColorUseCase
import io.github.mmolosay.thecolor.presentation.ColorInputColorStore
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

/**
 * Acts as mediator between ViewModels of different color input Views.
 * The responsibility of this component is to synchronize data between different color input Views.
 * It may also be used as distributor of color between color input Views.
 *
 * Once one View [send]s a [ColorInput], all other Views get the same color data through flows.
 * This way if user was using one specific View, after switching to other View they will see
 * the UI with the same data (color) they have left on in previous View.
 *
 * Any update is also sent to [colorInputColorStore], which can be used to obtain current abstract
 * color.
 *
 * Use overload of [send] method that accepts [Color] if you want to set color to color input Views
 * programmatically, not in response to user input via one of the color input Views.
 */
class ColorInputMediator @AssistedInject constructor(
    @Assisted private val colorInputColorStore: ColorInputColorStore,
    private val getInitialColor: GetInitialColorUseCase,
    private val colorInputMapper: ColorInputMapper,
    private val colorConverter: ColorConverter,
    private val colorInputFactory: ColorInputFactory,
) {

    private val colorStateFlow = MutableSharedFlow<ColorState?>(
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
        send(
            color = getInitialColor(),
            inputType = null,
        )
    }

    /**
     * TODO: add KDoc
     */
    suspend fun send(
        color: Color?,
        inputType: InputType?,
    ) {
        lastUsedInputType = inputType
        colorInputColorStore.updateWith(color)
        colorStateFlow.emit(color.toState())
    }

    private fun Color?.toState(): ColorState =
        if (this != null) {
            val abstractColor = with(colorConverter) { toAbstract() }
            ColorState.Valid(color = abstractColor)
        } else {
            ColorState.Invalid
        }

    /** State of the color the user is currently working with in color input View */
    private sealed interface ColorState {
        data object Invalid : ColorState // unfinished color
        data class Valid(val color: Color.Abstract) : ColorState
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

