package com.ordolabs.feature_home.ui.fragment.color.input.page

import com.ordolabs.thecolor.model.color.ColorPrototype

interface ColorInputParent {
    fun onInputChanged(input: ColorPrototype)
}