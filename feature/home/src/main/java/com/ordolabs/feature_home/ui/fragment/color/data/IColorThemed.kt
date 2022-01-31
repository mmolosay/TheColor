package com.ordolabs.feature_home.ui.fragment.color.data

import com.ordolabs.thecolor.model.color.Color

/**
 * Interface for fragment, which is ment to be shown on top of [color] and
 * be properly themed, to guarantee its content contrast against [color] background.
 */
interface IColorThemed {

    val color: Color?
}