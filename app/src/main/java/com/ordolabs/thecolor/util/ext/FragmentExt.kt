package com.ordolabs.thecolor.util.ext

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.github.michaelbull.result.Result
import com.ordolabs.thecolor.R
import com.ordolabs.thecolor.ui.fragment.BaseFragment
import com.ordolabs.thecolor.util.ContextUtil

fun Fragment.findFragmentById(
    @IdRes containerId: Int
): Result<Fragment, Throwable> {
    return ContextUtil.findFragmentById(
        this.childFragmentManager,
        containerId
    )
}

fun Fragment.findFragmentInDefaultContainer(): Result<Fragment, Throwable> {
    return ContextUtil.findFragmentById(
        this.childFragmentManager,
        R.id.defaultFragmentContainer
    )
}

fun Fragment.setFragment(
    fragment: BaseFragment,
    @IdRes containerId: Int = R.id.defaultFragmentContainer
): Result<Int, Throwable> {
    return ContextUtil.setFragment(
        this.childFragmentManager,
        fragment,
        containerId,
        fragment.transactionTag
    )
}

fun Fragment.replaceFragment(
    fragment: BaseFragment
): Result<Int, Throwable> {
    return ContextUtil.replaceFragment(
        this.childFragmentManager,
        fragment,
        R.id.defaultFragmentContainer,
        fragment.transactionTag
    )
}