package com.ordolabs.thecolor.util.ext

import android.widget.EditText
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.github.michaelbull.result.Result
import com.ordolabs.thecolor.ui.fragment.BaseFragment
import com.ordolabs.thecolor.util.ContextUtil

/**
 * Mostly uses methods of [ContextUtil] object.
 */

/***/ // eat file doc
val BaseFragment.shortAnimDuration: Long
    get() = ContextUtil.getShortAnimDuration(context) ?: 0L

val BaseFragment.mediumAnimDuration: Long
    get() = ContextUtil.getMediumAnimDuration(context) ?: 0L

val BaseFragment.longAnimDuration: Long
    get() = ContextUtil.getLongAnimDuration(context) ?: 0L

fun BaseFragment.findFragmentById(
    @IdRes containerId: Int = this.defaultFragmentContainerId
): Result<Fragment, Throwable> {
    return ContextUtil.findFragmentById(
        this.childFragmentManager,
        containerId
    )
}

fun BaseFragment.findFragmentByIdOrNull(
    @IdRes containerId: Int = this.defaultFragmentContainerId
): Fragment? {
    return ContextUtil.findFragmentByIdOrNull(
        this.childFragmentManager,
        containerId
    )
}

fun BaseFragment.setFragment(
    fragment: Fragment,
    @IdRes containerId: Int = this.defaultFragmentContainerId,
    transactionTag: String = fragment.getDefaultTransactionTag()
): Result<Int, Throwable> {
    return ContextUtil.setFragment(
        this.childFragmentManager,
        fragment,
        containerId,
        transactionTag
    )
}

fun BaseFragment.replaceFragment(
    fragment: Fragment,
    @IdRes containerId: Int = this.defaultFragmentContainerId,
    transactionTag: String = fragment.getDefaultTransactionTag()
): Result<Int, Throwable> {
    return ContextUtil.replaceFragment(
        this.childFragmentManager,
        fragment,
        containerId,
        transactionTag
    )
}

fun BaseFragment.removeFragment(
    fragment: Fragment
): Result<Int, Throwable> {
    return ContextUtil.removeFragment(
        this.childFragmentManager,
        fragment
    )
}

fun BaseFragment.removeFragment(
    @IdRes containerId: Int
): Result<Int, Throwable> {
    return ContextUtil.removeFragment(
        this.childFragmentManager,
        containerId
    )
}

fun Fragment.getDefaultTransactionTag(): String =
    this::class.java.simpleName

fun BaseFragment.showToast(
    text: String?,
    duration: Int = Toast.LENGTH_SHORT
): Boolean {
    val context = this.context ?: return false
    ContextUtil.showToast(context, text, duration)
    return true
}

fun BaseFragment.showToast(
    stringRes: Int?,
    duration: Int = Toast.LENGTH_SHORT
): Boolean {
    stringRes ?: return false
    val text = resources.getStringOrNull(stringRes) ?: return false
    return this.showToast(text, duration)
}

fun BaseFragment.showToastOfUnhandledError(): Boolean =
    this.showToast(this.defaultUnhandledErrorTextRes)

fun BaseFragment.hideSoftInput(): Boolean {
    val focused = this.view?.findFocus() ?: return false
    return if (focused is EditText) {
        focused.hideSoftInput()
    } else false
}

fun BaseFragment.hideSoftInputAndClearFocus(): Boolean {
    return this.hideSoftInput().also { wasHidden ->
        if (wasHidden) this.view?.clearFocus()
    }
}

fun BaseFragment.getChildFragmentAt(position: Int): Fragment? {
    return childFragmentManager.fragments.getOrNull(position)
}