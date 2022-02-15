package com.ordolabs.thecolor.util.ext

import android.widget.EditText
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.github.michaelbull.result.Result
import com.ordolabs.thecolor.di.AppComponent
import com.ordolabs.thecolor.ui.fragment.BaseFragment
import com.ordolabs.thecolor.util.ContextUtil

/**
 * Mostly maps methods of [ContextUtil] object.
 */

/***/ // eat file doc

// region Animation

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

// endregion

// region Fragments

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

// endregion

// region Toasts

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

// endregion

// region Soft input

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

// endregion

// region DI

val Fragment.appComponent: AppComponent
    get() = requireNotNull(ContextUtil.getAppComponent(context))

// endregion

// region ViewModels

// Actually, variations of Fragment.viewModels() function, but with more specific names
// in order to make inferring owners of ViewModels easier in code.

/**
 * Returns a property delegate to access ViewModel, scoped to `this` `Fragment`.
 *
 * @see [Fragment.viewModels]
 */
@MainThread
inline fun <reified VM : ViewModel> Fragment.ownViewModels(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> =
    this.viewModels(
        factoryProducer = factoryProducer
    )

/**
 * Returns a property delegate to access ViewModel, scoped to `this` [Fragment.getParentFragment].
 *
 * If `this` `Fragment` has no parent, then ViewModel will be
 * scoped (or created, if there is none) from `this` `Framgent`.
 *
 * @see [Fragment.viewModels]
 */
@MainThread
inline fun <reified VM : ViewModel> Fragment.parentViewModels(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> =
    this.viewModels(
        ownerProducer = { this.parentFragment ?: this },
        factoryProducer = factoryProducer
    )

/**
 * @see [Fragment.viewModels]
 */
@MainThread
inline fun <reified VM : ViewModel> Fragment.childViewModels(
    noinline ownerProducer: () -> ViewModelStoreOwner,
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> =
    this.viewModels(
        ownerProducer = ownerProducer,
        factoryProducer = factoryProducer
    )

// endregion

// region Fragments Result API

val Fragment.activityFragmentManager: FragmentManager
    get() = requireActivity().supportFragmentManager

// endregion