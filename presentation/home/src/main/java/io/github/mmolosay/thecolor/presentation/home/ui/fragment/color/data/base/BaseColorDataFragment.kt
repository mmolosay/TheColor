package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.base

import io.github.mmolosay.thecolor.presentation.fragment.BaseFragment

/**
 * Fragment that displays color data of type [D] and nothing more.
 * Has no functionality of independently obtaining data from outer source.
 */
abstract class BaseColorDataFragment<D> :
    BaseFragment(),
    ColorDataView<D>