package com.ordolabs.feature_home.ui.fragment

import androidx.annotation.LayoutRes
import com.ordolabs.feature_home.R
import com.ordolabs.thecolor.ui.fragment.BaseFragment as AppBaseFragment

abstract class BaseFragment(@LayoutRes layoutRes: Int) : AppBaseFragment(layoutRes) {

    override val defaultFragmentContainerId: Int = R.id.defaultFragmentContainer
}