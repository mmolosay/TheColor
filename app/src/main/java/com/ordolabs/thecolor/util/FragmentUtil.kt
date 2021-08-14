package com.ordolabs.thecolor.util

import androidx.fragment.app.Fragment
import com.github.michaelbull.result.Result
import com.ordolabs.thecolor.R
import com.ordolabs.thecolor.ui.fragment.BaseFragment

fun Fragment.setFragment(fragment: BaseFragment): Result<Int, Throwable> {
    return ContextUtil.setFragment(
        this.childFragmentManager,
        fragment,
        R.id.defaultFragmentContainer,
        fragment.transactionTag
    )
}

fun Fragment.replaceFragment(fragment: BaseFragment): Result<Int, Throwable> {
    return ContextUtil.replaceFragment(
        this.childFragmentManager,
        fragment,
        R.id.defaultFragmentContainer,
        fragment.transactionTag
    )
}