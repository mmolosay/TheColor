package com.ordolabs.feature_home.ui.fragment.colordata.base

import com.ordolabs.feature_home.ui.fragment.BaseFragment

/**
 * Fragment that displays passed in arguments color details and nothing more.
 * Has no functionality of independently obtaining data from outer source.
 */
abstract class BaseColorDataFragment<D> :
    BaseFragment(),
    IColorDataFragment<D>