package io.github.mmolosay.thecolor.demo.presentation.feature.recipe

import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionEnvironment
import javax.inject.Inject

// the screen's, the only environment that dependency injection builds
class RecipeEnvironment @Inject constructor(
    val editor: AmountEditorEnvironment,
    val nutrition: NutritionEnvironment,
)
