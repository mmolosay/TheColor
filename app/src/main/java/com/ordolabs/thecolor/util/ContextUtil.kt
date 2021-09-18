package com.ordolabs.thecolor.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.michaelbull.result.Result
import com.ordolabs.thecolor.ui.fragment.BaseFragment
import com.ordolabs.thecolor.util.ext.commit
import com.ordolabs.thecolor.util.ext.error
import com.ordolabs.thecolor.util.ext.success
import com.ordolabs.thecolor.util.ext.toResultOrError

object ContextUtil {

    fun getShortAnimDuration(context: Context?): Long? {
        context ?: return null
        return context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
    }

    fun getMediumAnimDuration(context: Context?): Long? {
        context ?: return null
        return context.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
    }

    fun getLongAnimDuration(context: Context?): Long? {
        context ?: return null
        return context.resources.getInteger(android.R.integer.config_longAnimTime).toLong()
    }

    fun findFragmentById(
        fm: FragmentManager,
        @IdRes containerId: Int
    ): Result<Fragment, Throwable> {
        return fm.findFragmentById(containerId)
            .toResultOrError { "no fragment with id=$containerId" }
    }

    fun findFragmentByTag(
        fm: FragmentManager,
        transactionTag: String
    ): Result<Fragment, Throwable> {
        return fm.findFragmentByTag(transactionTag)
            .toResultOrError { "no fragment with tag=$transactionTag" }
    }

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

    fun showToast(
        context: Context,
        text: String?,
        duration: Int
    ) {
        text ?: return
        Toast.makeText(context, text, duration).show()
    }
}