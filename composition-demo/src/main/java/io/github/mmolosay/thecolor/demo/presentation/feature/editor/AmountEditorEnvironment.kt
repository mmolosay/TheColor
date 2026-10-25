package io.github.mmolosay.thecolor.demo.presentation.feature.editor

import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.CupDialEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.GramsInputEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputEnvironment
import javax.inject.Inject

class AmountEditorEnvironment @Inject constructor(
    val grams: GramsInputEnvironment,
    val ounces: OuncesInputEnvironment,
    val cups: CupDialEnvironment,
    val amountConverter: AmountConverter,
)
