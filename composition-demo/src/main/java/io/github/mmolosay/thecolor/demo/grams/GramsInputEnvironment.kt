package io.github.mmolosay.thecolor.demo.grams

import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.textfield.TextFieldEnvironment
import javax.inject.Inject

class GramsInputEnvironment @Inject constructor(
    val textField: TextFieldEnvironment, // its Text Field's
    val amountConverter: AmountConverter,
)
