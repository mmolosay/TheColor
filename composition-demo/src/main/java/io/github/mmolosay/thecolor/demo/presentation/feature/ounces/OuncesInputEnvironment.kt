package io.github.mmolosay.thecolor.demo.presentation.feature.ounces

import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFormatter
import javax.inject.Inject

class OuncesInputEnvironment @Inject constructor(
    val amountConverter: AmountConverter,
) {
    // its Text Field's, carrying the rule for the text it allows, so that the field's own writes and
    // this feature's all keep it
    val textField = TextFieldEnvironment(formatter = ouncesTextFormatter)
}

// the digits after the point, both for what the user types and for an amount set from elsewhere
internal const val OuncesDecimalPlaces = 2

private const val MaxWholeDigits = 3

// Keeps only the text it allows: digits and one point, up to three digits before it, and no leading
// zeros except a single one before the point. A comma becomes the point, so the text holds a point
// whichever separator the setting shows
private val ouncesTextFormatter = TextFormatter { text ->
    val kept = text.replace(',', '.').filter { it.isDigit() || it == '.' }
    val point = kept.indexOf('.')
    val whole = run {
        val digits = if (point < 0) kept else kept.substring(0, point)
        val trimmed = digits.trimStart('0').take(MaxWholeDigits)
        if (trimmed.isEmpty() && (digits.isNotEmpty() || point >= 0)) "0" else trimmed
    }
    if (point < 0) return@TextFormatter whole
    val fraction = kept.substring(point + 1).filter { it != '.' }.take(OuncesDecimalPlaces)
    "$whole.$fraction"
}
