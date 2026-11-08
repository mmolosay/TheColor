package io.github.mmolosay.thecolor.demo.domain

import java.math.BigDecimal

data class NutritionFacts(
    val ingredient: Ingredient,
    val amount: Amount,
    val calories: Int,
    val protein: BigDecimal, // in grams
    val suggestedAmount: Amount, // the portion the service suggests instead; can be the same amount
)
