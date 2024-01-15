package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import javax.inject.Inject

class ValidateColorHexUseCase @Inject constructor() {

    private val hexColorValidationRegex: Regex by lazy {
        Regex("^([a-fA-F0-9]{6}|[a-fA-F0-9]{3})\$")
    }

    operator fun invoke(color: Color.Hex?): Boolean {
        color ?: return false
        return hexColorValidationRegex.matches(color.value)
    }
}