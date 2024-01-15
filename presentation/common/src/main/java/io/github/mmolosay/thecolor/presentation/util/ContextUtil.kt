package io.github.mmolosay.thecolor.presentation.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getOrElse
import io.github.mmolosay.thecolor.presentation.util.ext.commit
import io.github.mmolosay.thecolor.presentation.util.ext.error
import io.github.mmolosay.thecolor.presentation.util.ext.success
import io.github.mmolosay.thecolor.presentation.util.ext.toResultOrError

object ContextUtil {

    // region Animation

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

    // endregion

    // region Fragments

    fun findFragmentById(
        fm: FragmentManager,
        @IdRes containerId: Int
    ): Result<Fragment, Throwable> {
        return fm.findFragmentById(containerId)
            .toResultOrError { "no fragment with id=$containerId" }
    }

    fun findFragmentByIdOrNull(
        fm: FragmentManager,
        @IdRes containerId: Int
    ): Fragment? {
        return findFragmentById(fm, containerId).get()
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
        fragment: Fragment,
        @IdRes containerId: Int,
        transactionTag: String?
    ): Result<Int, Throwable> {
        fm.findFragmentById(containerId)?.let {
            return Result.error("fragment with $containerId id already added")
        }
        val transactionId = fm.commit {
            add(containerId, fragment, transactionTag)
        }
        return Result.success(transactionId)
    }

    @Suppress("UNCHECKED_CAST")
    fun <F : Fragment> setFragmentOrGet(
        fm: FragmentManager,
        @IdRes containerId: Int,
        transactionTag: String?,
        fragmentProducer: () -> F
    ): F {
        fm.findFragmentById(containerId)?.let { added ->
            return added as F
        }
        val fragment = fragmentProducer()
        fm.commit {
            add(containerId, fragment, transactionTag)
        }
        return fragment
    }

    fun replaceFragment(
        fm: FragmentManager,
        fragment: Fragment,
        @IdRes containerId: Int,
        transactionTag: String
    ): Result<Int, Throwable> {
        val transactionId = fm.commit {
            replace(containerId, fragment, transactionTag)
        }
        return Result.success(transactionId)
    }

    fun removeFragment(
        fm: FragmentManager,
        fragment: Fragment
    ): Result<Int, Throwable> {
        if (!fm.fragments.contains(fragment)) {
            return Result.error("fragment $fragment in not associated with fragment manager")
        }
        val transactionId = fm.commit {
            remove(fragment)
        }
        return Result.success(transactionId)
    }

    fun removeFragment(
        fm: FragmentManager,
        @IdRes containerId: Int
    ): Result<Int, Throwable> {
        val fragment = findFragmentById(fm, containerId).getOrElse { return Result.error(it) }
        return removeFragment(fm, fragment)
    }

    // endregion

    // region Toast

    fun showToast(
        context: Context,
        text: String?,
        duration: Int
    ) {
        text ?: return
        Toast.makeText(context, text, duration).show()
    }

    // endregion
}