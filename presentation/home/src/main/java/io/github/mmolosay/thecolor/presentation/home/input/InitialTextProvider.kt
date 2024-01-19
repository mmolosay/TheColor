package io.github.mmolosay.thecolor.presentation.home.input

import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData.Text
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides text to be set in input fields of color input View on initialization.
 * Was introduced to make testing easier.
 */
@Singleton
class InitialTextProvider @Inject constructor() {
    // there's no initial color to be populated
    val hex = Text("")
    val rgbR = Text("")
    val rgbG = Text("")
    val rgbB = Text("")
}