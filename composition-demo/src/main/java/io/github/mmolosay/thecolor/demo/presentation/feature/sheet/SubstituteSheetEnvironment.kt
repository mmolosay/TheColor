package io.github.mmolosay.thecolor.demo.presentation.feature.sheet

import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionEnvironment
import javax.inject.Inject

class SubstituteSheetEnvironment @Inject constructor(
    val nutrition: NutritionEnvironment,
)
