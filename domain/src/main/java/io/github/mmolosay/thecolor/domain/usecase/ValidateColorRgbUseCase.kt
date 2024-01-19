package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import javax.inject.Inject

// TODO: delete; now Color.Rgb is valid by definition
class ValidateColorRgbUseCase @Inject constructor() {

    private val componentRange by lazy { 0..255 }

    operator fun invoke(color: Color.Rgb?): Boolean {
        color ?: return false
//        val components = listOf(color.r, color.b, color.g)
//        return components.all { it in componentRange }
        return true
    }
}