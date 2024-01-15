package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.input.page

import io.github.mmolosay.thecolor.presentation.color.ColorPrototype

interface ColorInputParent {
    fun onInputChanged(input: ColorPrototype)
}