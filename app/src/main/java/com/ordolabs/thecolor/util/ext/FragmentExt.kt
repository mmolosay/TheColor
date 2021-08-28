package com.ordolabs.thecolor.util.ext

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.github.michaelbull.result.Result
import com.ordolabs.thecolor.ui.fragment.BaseFragment
import com.ordolabs.thecolor.util.ContextUtil

fun BaseFragment.findFragmentById(
    @IdRes containerId: Int = this.defaultFragmentContainerId
): Result<Fragment, Throwable> {
    return ContextUtil.findFragmentById(
        this.childFragmentManager,
        containerId
    )
}

fun BaseFragment.setFragment(
    fragment: BaseFragment,
    @IdRes containerId: Int = this.defaultFragmentContainerId
): Result<Int, Throwable> {
    return ContextUtil.setFragment(
        this.childFragmentManager,
        fragment,
        containerId,
        fragment.transactionTag
    )
}

fun BaseFragment.replaceFragment(
    fragment: BaseFragment,
    @IdRes containerId: Int = this.defaultFragmentContainerId
): Result<Int, Throwable> {
    return ContextUtil.replaceFragment(
        this.childFragmentManager,
        fragment,
        containerId,
        fragment.transactionTag
    )
}