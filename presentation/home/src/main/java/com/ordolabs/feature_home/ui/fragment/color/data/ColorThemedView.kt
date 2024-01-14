package com.ordolabs.feature_home.ui.fragment.color.data

import io.github.mmolosay.presentation.model.color.Color

/**
 * Interface for `View`, which is ment to be shown on top of [color] and
 * be properly themed, to guarantee its UI contrast against [color] background.
 */
interface ColorThemedView {

    val color: Color?
}