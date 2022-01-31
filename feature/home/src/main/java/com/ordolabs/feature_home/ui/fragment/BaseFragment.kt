package com.ordolabs.feature_home.ui.fragment

import androidx.annotation.LayoutRes
import com.ordolabs.feature_home.R
import com.ordolabs.thecolor.ui.fragment.BaseFragment as AppBaseFragment

abstract class BaseFragment : AppBaseFragment {

    constructor() : super()

    constructor(@LayoutRes layoutRes: Int) : super(layoutRes)

    override val defaultFragmentContainerId: Int = R.id.defaultFragmentContainer
}