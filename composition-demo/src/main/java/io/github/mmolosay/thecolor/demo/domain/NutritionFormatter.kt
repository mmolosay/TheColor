package io.github.mmolosay.thecolor.demo.domain

import java.math.BigDecimal

// for what Views show, through the cores; follows the user's language
interface NutritionFormatter {
    fun calories(calories: Int): String // "437 kcal"
    fun protein(protein: BigDecimal): String // "12.4 g protein"
}
