package io.github.mmolosay.thecolor.demo.domain

// for what Views show, through the cores; follows the user's language
interface AmountFormatter {
    fun format(amount: Amount): String // "120 g", "4.23 oz", "1 2/4 cups"
}
