package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.model.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import io.github.mmolosay.thecolor.domain.model.ColorInputType as DomainColorInputType

/**
 * Acts as a mediator between ViewModels of different 'Color Input' types.
 *
 * The responsibility of this component is to be a single source of truth regarding the color the user
 * currently works with in the 'Color Input' View(s).
 * This helps to synchronize the data between all types of 'Color Input'.
 * This class may also be used to set a specific color to all 'Color Input' types.
 */
class ColorInputMediator @Inject constructor() {

    private val _colorStateFlow = MutableStateFlow(InitialColorStateWithSource)
    val colorStateFlow: StateFlow<ColorStateWithSource> = _colorStateFlow.asStateFlow()

    /**
     * Exposes the specified [color] from the [colorStateFlow].
     *
     * @param color The new [Color] to be set, or `null` if the color should be erased.
     * @param from The type of 'Color Input' that triggered this update, or `null` if the
     * update was triggered programmatically.
     */
    // TODO: rename to "set"
    fun send(
        color: Color?,
        from: DomainColorInputType?,
    ) {
        _colorStateFlow.update {
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

    companion object {
        val InitialColorStateWithSource = ColorStateWithSource(
            colorState = ColorState.AbsentOrInvalid,
            sourceInputType = null,
        )
    }
}

fun ColorInputMediator.ColorState.colorOrNull(): Color? =
    this.let { it as? ColorInputMediator.ColorState.Valid }?.color