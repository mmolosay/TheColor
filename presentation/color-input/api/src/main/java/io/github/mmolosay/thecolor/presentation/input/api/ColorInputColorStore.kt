package io.github.mmolosay.thecolor.presentation.input.api

import io.github.mmolosay.thecolor.domain.model.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Storage that holds current valid color from Color Input feature.
 *
 * Its [set] method is called only to reflect the present color in Color Input.
 * If you want to set new color to Color Input, use [ColorInputMediator][io.github.mmolosay.thecolor.presentation.input.impl.ColorInputMediator].
 */
class ColorInputColorStore @Inject constructor() : ColorInputColorProvider {

    private val _colorFlow = MutableStateFlow<Color?>(null)
    override val colorFlow: StateFlow<Color?> = _colorFlow.asStateFlow()

    fun set(color: Color?) {
        _colorFlow.value = color
    }
}

/** Read-only provider. */
interface ColorInputColorProvider {
    val colorFlow: StateFlow<Color?>
}