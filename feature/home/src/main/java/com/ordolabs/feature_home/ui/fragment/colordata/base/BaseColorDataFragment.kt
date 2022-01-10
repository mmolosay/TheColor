package com.ordolabs.feature_home.ui.fragment.colordata.base

import com.ordolabs.feature_home.ui.fragment.BaseFragment

/**
 * Fragment that displays color data, passed in arguments, and nothing more.
 * Has no functionality of independently obtaining data from outer source.
 */
abstract class BaseColorDataFragment<D> :
    BaseFragment(),
    IColorDataFragment<D>