package com.ordolabs.thecolor.util.ext

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.github.michaelbull.result.Result
import com.ordolabs.thecolor.util.ContextUtil

fun AppCompatActivity.setFragment(
    fragment: Fragment,
    @IdRes containerId: Int
): Result<Int, Throwable> {
    return ContextUtil.setFragment(
        this.supportFragmentManager,
        fragment,
        containerId,
        fragment.getDefaultTransactionTag()
    )
}

fun AppCompatActivity.replaceFragment(
    fragment: Fragment,
    @IdRes containerId: Int
): Result<Int, Throwable> {
    return ContextUtil.replaceFragment(
        this.supportFragmentManager,
        fragment,
        containerId,
        fragment.getDefaultTransactionTag()
    )
}

