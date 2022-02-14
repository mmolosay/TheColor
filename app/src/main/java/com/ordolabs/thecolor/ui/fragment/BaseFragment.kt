package com.ordolabs.thecolor.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ordolabs.thecolor.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

abstract class BaseFragment : Fragment {

    constructor()

    constructor(@LayoutRes layoutRes: Int) : super(layoutRes)

    @IdRes
    open val defaultFragmentContainerId: Int = R.id.defaultFragmentContainer

    @StringRes
    open val defaultUnhandledErrorTextRes = R.string.error_unhandled

    /**
     * Initial soft input mode, which `activity` had before adding `this` fragment.
     */
    protected val initialSoftInputMode: Int? by lazy {
        activity?.window?.attributes?.softInputMode
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSoftInputMode // initialize
        updateSoftInputMode()
        setUp()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectViewModelsData()
        setFragments()
        setViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        restoreSoftInputMode()
    }

    // region Fragment.onCreate

    /**
     * Configures non-view components.
     * Being called in [Fragment.onCreate] method.
     */
    protected open fun setUp() {
        // default empty implementation
    }

    private fun updateSoftInputMode() {
        getSoftInputMode()?.let {
            activity?.window?.setSoftInputMode(it)
        }
    }

    // endregion

    // region Fragment.onViewCreated

    /**
     * Collects (subscribes to) data from ViewModel's.
     * Being called in [Fragment.onViewCreated] method.
     */
    protected open fun collectViewModelsData() {
        // default empty implementation
    }

    /**
     * Configures child fragments.
     * Being called in [Fragment.onViewCreated] method.
     */
    protected open fun setFragments() {
        // default empty implementation
    }

    /**
     * Sets fragment's views and configures them.
     * Being called in [Fragment.onViewCreated] method.
     */
    protected abstract fun setViews()

    // endregion

    // region Fragment.onDestroy

    private fun restoreSoftInputMode() {
        initialSoftInputMode?.let {
            activity?.window?.setSoftInputMode(it)
        }
    }

    // endregion

    // region Non-lifecycle methods

    /**
     * Specifies windowSoftInputMode for `this` fragment.
     */
    protected open fun getSoftInputMode(): Int? =
        null

    // endregion

    companion object {
        // extra keys and stuff
    }

    /**
     * @see launch
     * @see Lifecycle.repeatOnLifecycle
     * @see Flow.collect
     */
    protected inline fun <T> Flow<T>.collectOnLifecycle(
        state: Lifecycle.State = Lifecycle.State.STARTED,
        crossinline action: suspend (value: T) -> Unit
    ) =
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(state) {
                this@collectOnLifecycle.collect(action)
            }
        }
}