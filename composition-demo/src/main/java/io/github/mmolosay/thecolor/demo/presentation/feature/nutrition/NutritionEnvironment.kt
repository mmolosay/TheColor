package io.github.mmolosay.thecolor.demo.presentation.feature.nutrition

import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.domain.AmountFormatter
import io.github.mmolosay.thecolor.demo.domain.NutritionFormatter
import javax.inject.Inject

class NutritionEnvironment @Inject constructor(
    val amountConverter: AmountConverter,
    val amountFormatter: AmountFormatter,
    val nutritionFormatter: NutritionFormatter,
)
