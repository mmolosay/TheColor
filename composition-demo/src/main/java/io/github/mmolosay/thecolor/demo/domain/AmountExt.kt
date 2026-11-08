package io.github.mmolosay.thecolor.demo.domain

// the same amount in any form, compared in whole grams: 85 g, 3 oz and 3.00 oz are the same;
// 42 g and 1.5 oz aren't, since 1.5 oz is 42.52 g, so 43 g
context(converter: AmountConverter)
infix fun Amount?.isSameAs(other: Amount?): Boolean {
    val amount = this
    // no amount is the same as no amount, and not the same as an amount
    if (amount == null || other == null) return (amount == other)
    return with(converter) { amount.toGrams() == other.toGrams() }
}
