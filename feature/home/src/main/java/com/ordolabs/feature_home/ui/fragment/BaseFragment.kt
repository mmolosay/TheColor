package com.ordolabs.feature_home.ui.fragment

import com.ordolabs.feature_home.R
import com.ordolabs.thecolor.ui.fragment.BaseFragment as AppBaseFragment

abstract class BaseFragment : AppBaseFragment() {

    override val defaultFragmentContainerId: Int = R.id.defaultFragmentContainer
}