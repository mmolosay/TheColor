package io.github.mmolosay.thecolor.demo.presentation.format

import io.github.mmolosay.thecolor.demo.domain.NutritionFormatter
import java.math.BigDecimal
import java.text.NumberFormat
import javax.inject.Inject

// numbers in the user's locale; units in English, as the rest of the demo
class LocaleNutritionFormatter @Inject constructor() : NutritionFormatter {

    override fun calories(calories: Int): String =
        "${NumberFormat.getIntegerInstance().format(calories)} kcal"

    override fun protein(protein: BigDecimal): String =
        "${NumberFormat.getNumberInstance().format(protein)} g protein"
}
