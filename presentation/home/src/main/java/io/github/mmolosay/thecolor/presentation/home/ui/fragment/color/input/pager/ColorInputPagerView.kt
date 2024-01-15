package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.input.pager

import io.github.mmolosay.thecolor.presentation.color.Color

interface ColorInputPagerView {
    fun updateCurrentColor(color: Color)
    fun clearCurrentColor()
}