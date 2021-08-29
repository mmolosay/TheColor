package com.ordolabs.thecolor.util.ext

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import com.github.michaelbull.result.Result
import com.ordolabs.thecolor.ui.fragment.BaseFragment
import com.ordolabs.thecolor.util.ContextUtil

fun AppCompatActivity.setFragment(
    fragment: BaseFragment,
    @IdRes containerId: Int
): Result<Int, Throwable> {
    return ContextUtil.setFragment(
        this.supportFragmentManager,
        fragment,
        containerId,
        fragment.transactionTag
    )
}

fun AppCompatActivity.replaceFragment(
    fragment: BaseFragment,
    @IdRes containerId: Int
): Result<Int, Throwable> {
    return ContextUtil.replaceFragment(
        this.supportFragmentManager,
        fragment,
        containerId,
        fragment.transactionTag
    )
}

