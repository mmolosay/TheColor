package io.github.mmolosay.thecolor.presentation

import io.github.mmolosay.thecolor.domain.model.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Storage that holds current valid color from color input View.
 */
@Singleton
class ColorInputColorStore @Inject constructor() : ColorInputColorProvider {

    private val _colorFlow = MutableStateFlow<Color?>(null)
    override val colorFlow: StateFlow<Color?> = _colorFlow.asStateFlow()

    fun updateWith(color: Color?) {
        _colorFlow.value = color
    }
}

/** Read-only provider. */
interface ColorInputColorProvider {
    val colorFlow: StateFlow<Color?>
}