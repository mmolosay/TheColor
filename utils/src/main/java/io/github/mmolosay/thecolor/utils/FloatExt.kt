package io.github.mmolosay.thecolor.utils

import java.math.BigDecimal
import java.math.RoundingMode

fun Float.decimalPlaces(): Int {
    val scale = this.toBigDecimal()
        .stripTrailingZeros()
        .scale()
    return maxOf(0, scale)
}

fun Float.truncateDecimalPlaces(keep: Int): Float {
    require(keep >= 0) { "number of decimal places to keep must be non-negative" }
    return BigDecimal.valueOf(this.toDouble())
        .setScale(keep, RoundingMode.DOWN)
        .toFloat()
}