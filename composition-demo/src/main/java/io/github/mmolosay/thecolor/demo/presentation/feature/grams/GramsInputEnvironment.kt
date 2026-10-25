package io.github.mmolosay.thecolor.demo.presentation.feature.grams

import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFormatter
import javax.inject.Inject

class GramsInputEnvironment @Inject constructor(
    val amountConverter: AmountConverter,
) {
    // its Text Field's, carrying the rule for the text it allows, so that the field's own writes and
    // this feature's all keep it
    val textField = TextFieldEnvironment(formatter = gramsTextFormatter)
}

// keeps only the text it allows: digits, up to four, no leading zeros
private val gramsTextFormatter = TextFormatter { text ->
    text.filter { it in '0'..'9' }.trimStart('0').take(4)
}
