package com.ordolabs.feature_home.ui.fragment.color.input.pager

import com.ordolabs.thecolor.model.color.Color

interface ColorInputPagerView {
    fun updateCurrentColor(color: Color)
    fun clearCurrentColor()
}