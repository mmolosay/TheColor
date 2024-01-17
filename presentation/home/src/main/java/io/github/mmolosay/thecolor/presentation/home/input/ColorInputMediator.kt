package io.github.mmolosay.thecolor.presentation.home.input

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.ValidateColorHexUseCase
import io.github.mmolosay.thecolor.presentation.color.ColorPrototype
import io.github.mmolosay.thecolor.presentation.mapper.toDomainOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

/**
 * Acts as mediator between ViewModels of different color input Views.
 * The responsibility of this component is to synchronize data between different color input Views,
 * so if user was using once specific View, after switching to any other they see the same data (same color).
 *
 * The pipeline is following:
 * 1. ViewModel receives user input (e.g. new text in text field).
 * 2. ViewModel converts all data from user input into [ColorPrototype] and passes it to [update].
 * 3. Passed [ColorPrototype] is turned to domain [Color.Abstract] and is put into [colorFlow].
 * 4. Color-space-oriented flows [hexFlow] and [rgbFlow] map [colorFlow] into respective color spaces.
 * 5. ViewModels subscribe to flow of their color space.
 * 6. Once this flow emits, it means that user has entered valid color in some color input View.
 *    All other Views should populate this color in their UI.
 *    This way if user switches to other color input View, they can continue editing the color they
 *    left on in previous color input View.
 */
class ColorInputMediator @Inject constructor(
    private val validateHexColor: ValidateColorHexUseCase,
    private val colorConverter: ColorConverter,
) {

    private val colorFlow = MutableStateFlow<Color.Abstract?>(null)

    val hexFlow: Flow<Color.Hex> =
        colorFlow.mapNotNull { color ->
            with(colorConverter) { color?.toHex() }
        }
    val rgbFlow: Flow<Color.Rgb> =
        colorFlow.mapNotNull { color ->
            with(colorConverter) { color?.toRgb() }
        }

    fun update(new: ColorPrototype) {
        val abstract = new.toAbstract() ?: return // ignore unfinished colors
        colorFlow.value = abstract
    }

    private fun ColorPrototype.toAbstract(): Color.Abstract? =
        when (this) {
            is ColorPrototype.Hex -> toAbstract()
            is ColorPrototype.Rgb -> toAbstract()
        }

    private fun ColorPrototype.Hex.toAbstract(): Color.Abstract? {
        val color: Color.Hex = this.toDomainOrNull() ?: return null // ignore unfinished colors
        return with(colorConverter) { color.toAbstract() }
    }

    private fun ColorPrototype.Rgb.toAbstract(): Color.Abstract? {
        val color: Color.Rgb = this.toDomainOrNull() ?: return null // ignore unfinished colors
        return with(colorConverter) { color.toAbstract() }
    }
}