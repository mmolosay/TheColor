package io.github.mmolosay.thecolor.demo.presentation.feature.recipe

import io.github.mmolosay.thecolor.demo.domain.AmountFormatter
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheetEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.substitutes.SubstitutesEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFormatter
import javax.inject.Inject

// the screen's, the only environment that dependency injection builds
class RecipeEnvironment @Inject constructor(
    val editor: AmountEditorEnvironment,
    val nutrition: NutritionEnvironment,
    val substitutes: SubstitutesEnvironment,
    val sheet: SubstituteSheetEnvironment,
    val amountFormatter: AmountFormatter,
) {
    // the servings' Text Field's, carrying the rule for the text it allows
    val servings = TextFieldEnvironment(formatter = servingsTextFormatter)
}

// keeps only the text it allows: a number of servings from 1 to 99, or nothing while it's typed
private val servingsTextFormatter = TextFormatter { text ->
    text.filter { it in '0'..'9' }.trimStart('0').take(2)
}
