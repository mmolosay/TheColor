package io.github.mmolosay.thecolor.demo.domain

import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class AmountConverter @Inject constructor() {

    // rounded to whole grams
    fun Amount.toGrams(): Amount.Grams {
        val grams = this.exactGrams().setScale(0, RoundingMode.HALF_UP)
        return Amount.Grams(value = grams.intValueExact())
    }

    fun Amount.toOunces(decimalPlaces: Int): Amount.Ounces {
        val ounces = this.exactGrams().divide(GramsPerOunce, decimalPlaces, RoundingMode.HALF_UP)
        return Amount.Ounces(value = ounces)
    }

    // the nearest position with that denominator
    fun Amount.toCups(denominator: Int): Amount.Cups {
        val parts = run {
            val cups = this.exactGrams().divide(GramsPerCup, Scale, RoundingMode.HALF_UP)
            val exactParts = cups.multiply(BigDecimal(denominator))
            exactParts.setScale(0, RoundingMode.HALF_UP).intValueExact()
        }
        return Amount.Cups(
            whole = parts / denominator,
            numerator = parts % denominator,
            denominator = denominator,
        )
    }

    private fun Amount.exactGrams(): BigDecimal =
        when (this) {
            is Amount.Grams -> BigDecimal(this.value)
            is Amount.Ounces -> this.value.multiply(GramsPerOunce)
            is Amount.Cups -> {
                val parts = BigDecimal(this.whole * this.denominator + this.numerator)
                val partGrams = run {
                    val denominator = BigDecimal(this.denominator)
                    GramsPerCup.divide(denominator, Scale, RoundingMode.HALF_UP)
                }
                parts.multiply(partGrams)
            }
        }

    private companion object {
        val GramsPerOunce = BigDecimal("28.3495")
        val GramsPerCup = BigDecimal(120) // of flour
        const val Scale = 10 // for divisions that don't end, such as a third of a cup
    }
}
