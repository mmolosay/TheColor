package io.github.mmolosay.thecolor.presentation.input

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import io.github.mmolosay.thecolor.domain.model.ColorInputType as DomainColorInputType

/**
 * Acts as a mediator between ViewModels of different 'Color Input' types.
 *
 * The responsibility of this component is to be a single source of truth regarding the color the user
 * currently works with in the 'Color Input' View(s).
 * This helps to synchronize the data between all types of 'Color Input'.
 * This class may also be used to set a specific color to all 'Color Input' types.
 *
 * Any update is also sent to [colorInputColorStore], which can be used to obtain current
 * color.
 */
class ColorInputMediator @AssistedInject constructor(
    @Assisted private val colorInputColorStore: ColorInputColorStore,
) {

    private val _colorStateFlow: MutableStateFlow<ColorStateWithSource> = run {
        val value = ColorStateWithSource(
            colorState = ColorState.AbsentOrInvalid,
            sourceInputType = null,
        )
        MutableStateFlow(value)
    }
    val colorStateFlow: StateFlow<ColorStateWithSource> = _colorStateFlow.asStateFlow()

    /**
     * Propagates specified [color] to color input flows (e.g. [hexColorInputFlow]).
     * Parameter [from] defines which color input flow will NOT receive an update to avoid
     * update loop.
     *
     * Passing `null` [color] will emit empty [ColorInput]s from flows.
     * Passing `null` [from] will not ignore any flow and all of them will emit.
     */
    fun send(
        color: Color?,
        from: DomainColorInputType?,
    ) {
        _colorStateFlow.update {
            colorInputColorStore.set(color)
            return@update ColorStateWithSource(
                colorState = color.toState(),
                sourceInputType = from
            )
        }
    }

    private fun Color?.toState(): ColorState =
        if (this != null) {
            ColorState.Valid(color = this)
        } else {
            ColorState.AbsentOrInvalid
        }

    /** State of the color the user is currently working with in 'Color Input' View */
    sealed interface ColorState {
        data object AbsentOrInvalid : ColorState
        data class Valid(val color: Color) : ColorState
    }

    /**
     * Couples a [ColorState] with the source [DomainColorInputType] it originates from.
     * When the [sourceInputType] is `null`, it means that this color update didn't come
     * from any particular 'Color Input' type but was set programmatically.
     */
    data class ColorStateWithSource(
        val colorState: ColorState,
        val sourceInputType: DomainColorInputType?,
    )

    @AssistedFactory
    fun interface Factory {
        fun create(
            colorInputColorStore: ColorInputColorStore,
        ): ColorInputMediator
    }
}

// TODO: remove me
fun ColorInputMediator.ColorState.colorOrNull(): Color? =
    this.let { it as? ColorInputMediator.ColorState.Valid }?.color