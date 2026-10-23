package io.github.mmolosay.thecolor.demo.presentation.feature.recipe

import io.github.mmolosay.thecolor.demo.domain.AmountFormatter
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheetEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.substitutes.SubstitutesEnvironment
import javax.inject.Inject

// the screen's, the only environment that dependency injection builds
class RecipeEnvironment @Inject constructor(
    val editor: AmountEditorEnvironment,
    val nutrition: NutritionEnvironment,
    val substitutes: SubstitutesEnvironment,
    val sheet: SubstituteSheetEnvironment,
    val amountFormatter: AmountFormatter,
)
