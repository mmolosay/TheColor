package com.ordolabs.feature_home.ui.fragment.color.input.page

import io.github.mmolosay.presentation.model.color.ColorPrototype

interface ColorInputParent {
    fun onInputChanged(input: ColorPrototype)
}