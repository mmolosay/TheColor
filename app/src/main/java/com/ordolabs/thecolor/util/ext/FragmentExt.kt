package com.ordolabs.thecolor.util.ext

import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.github.michaelbull.result.Result
import com.ordolabs.thecolor.ui.fragment.BaseFragment
import com.ordolabs.thecolor.util.ContextUtil

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

fun BaseFragment.showToast(
    text: String?,
    duration: Int = Toast.LENGTH_SHORT
) {
    ContextUtil.showToast(requireContext(), text, duration)
}

fun BaseFragment.showToast(
    @StringRes textRes: Int?,
    duration: Int = Toast.LENGTH_SHORT
) {
    textRes ?: return
    val text = getString(textRes)
    ContextUtil.showToast(requireContext(), text, duration)
}