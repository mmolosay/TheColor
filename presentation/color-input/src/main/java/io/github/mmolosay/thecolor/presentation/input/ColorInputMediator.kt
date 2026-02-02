package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.model.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _colorStateFlow = MutableStateFlow(InitialColorState)
    val colorStateFlow: StateFlow<ColorState> = _colorStateFlow.asStateFlow()

    /**
     * Exposes the specified [color] from the [colorStateFlow].
     *
     * @param color The new [Color] to be set, or `null` if the color should be erased.
     * @param source The type of 'Color Input' that triggered this update, or `null` if the
     * update was triggered programmatically.
     */
    fun set(
        color: Color?,
        source: DomainColorInputType?,
    ) {
        _colorStateFlow.value = ColorState(color = color, source = source)
    }

    /**
     * State of the color across the 'Color Input' feature.
     *
     * @param color the current color. `null` means that the color is absent or invalid.
     * @param source the type of the 'Color Input' this [color] originates from.
     * `null` means that this [color] didn't come from any particular type of the 'Color Input' but was set programmatically.
     */
    data class ColorState(
        val color: Color?,
        val source: DomainColorInputType?,
    )

    companion object {
        val InitialColorState = ColorState(color = null, source = null)
    }
}