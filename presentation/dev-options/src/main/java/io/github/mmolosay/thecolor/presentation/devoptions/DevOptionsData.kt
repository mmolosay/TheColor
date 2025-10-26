package io.github.mmolosay.thecolor.presentation.devoptions

import io.github.mmolosay.thecolor.domain.model.DevOptions.PredictableRandomColors as DomainPredictableRandomColors

/**
 * Platform-agnostic data provided by ViewModel to 'Developer Options' View.
 */
data class DevOptionsData(
    val predictableRandomColors: DomainPredictableRandomColors, // it's OK to use some domain models (like enums) in presentation layer
    val changePredictableRandomColors: (DomainPredictableRandomColors) -> Unit,
)