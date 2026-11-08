package io.github.mmolosay.thecolor.demo.domain

import java.math.BigDecimal

// an amount of an ingredient, in the form of whatever produced it
sealed interface Amount {

    data class Grams(
        val value: Int,
    ) : Amount

    data class Ounces(
        val value: BigDecimal, // 1.5 and 1.50 are different values here
    ) : Amount

    // 1 2/4 cups is 'Cups(whole = 1, numerator = 2, denominator = 4)'
    data class Cups(
        val whole: Int,
        val numerator: Int,
        val denominator: Int,
    ) : Amount
}
