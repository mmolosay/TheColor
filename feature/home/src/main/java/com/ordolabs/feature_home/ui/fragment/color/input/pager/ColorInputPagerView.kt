package com.ordolabs.feature_home.ui.fragment.color.input.pager

import io.github.mmolosay.presentation.model.color.Color

interface ColorInputPagerView {
    fun updateCurrentColor(color: Color)
    fun clearCurrentColor()
}