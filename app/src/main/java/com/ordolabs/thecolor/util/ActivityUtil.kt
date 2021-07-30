package com.ordolabs.thecolor.util

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import com.ordolabs.thecolor.R
import com.ordolabs.thecolor.ui.fragment.BaseFragment
import java.lang.Exception

fun AppCompatActivity.setFragment(fragment: BaseFragment): Result<Int> {
    return setFragment(fragment, R.id.defaultFragmentContainer, fragment.transactionTag)
}

fun AppCompatActivity.setFragment(
    fragment: BaseFragment,
    @IdRes containerId: Int,
    transactionTag: String
): Result<Int> {
    supportFragmentManager.findFragmentByTag(transactionTag)?.let {
        return Result.failure(Exception("fragment with $transactionTag tag already added"))
    }
    val transactionId = supportFragmentManager.commit {
        add(containerId, fragment, transactionTag)
    }
    return Result.success(transactionId)
}
