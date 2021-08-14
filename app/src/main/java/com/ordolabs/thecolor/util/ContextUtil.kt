package com.ordolabs.thecolor.util

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import com.github.michaelbull.result.Result
import com.ordolabs.thecolor.ui.fragment.BaseFragment

object ContextUtil {

    fun setFragment(
        fm: FragmentManager,
        fragment: BaseFragment,
        @IdRes containerId: Int,
        transactionTag: String
    ): Result<Int, Throwable> {
        fm.findFragmentByTag(transactionTag)?.let {
            return Result.error("fragment with $transactionTag tag already added")
        }
        val transactionId = fm.commit {
            add(containerId, fragment, transactionTag)
        }
        return Result.success(transactionId)
    }

    fun replaceFragment(
        fm: FragmentManager,
        fragment: BaseFragment,
        @IdRes containerId: Int,
        transactionTag: String
    ): Result<Int, Throwable> {
        fm.findFragmentByTag(transactionTag)?.let {
            return Result.error("fragment with $transactionTag tag already added")
        }
        val transactionId = fm.commit {
            replace(containerId, fragment, transactionTag)
        }
        return Result.success(transactionId)
    }
}