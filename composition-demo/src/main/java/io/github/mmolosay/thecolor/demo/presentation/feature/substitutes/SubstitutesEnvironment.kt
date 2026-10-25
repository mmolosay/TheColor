package io.github.mmolosay.thecolor.demo.presentation.feature.substitutes

import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.domain.NutritionFormatter
import javax.inject.Inject

class SubstitutesEnvironment @Inject constructor(
    val amountConverter: AmountConverter,
    val nutritionFormatter: NutritionFormatter,
)
