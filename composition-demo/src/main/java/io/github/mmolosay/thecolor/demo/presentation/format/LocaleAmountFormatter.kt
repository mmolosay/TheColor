package io.github.mmolosay.thecolor.demo.presentation.format

import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.AmountFormatter
import java.text.NumberFormat
import javax.inject.Inject

// numbers in the user's locale; units in English, as the rest of the demo
class LocaleAmountFormatter @Inject constructor() : AmountFormatter {

    override fun format(amount: Amount): String =
        when (amount) {
            is Amount.Grams -> "${amount.value} g"
            is Amount.Ounces -> "${NumberFormat.getNumberInstance().format(amount.value)} oz"
            is Amount.Cups -> amount.format()
        }

    // "2 cups", "3/4 cup", "1 2/4 cups"
    private fun Amount.Cups.format(): String {
        val fraction = "${this.numerator}/${this.denominator}"
        return when {
            (this.numerator == 0) -> "${this.whole} cups"
            (this.whole == 0) -> "$fraction cup"
            else -> "${this.whole} $fraction cups"
        }
    }
}
