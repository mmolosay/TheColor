package com.ordolabs.thecolor.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStoreOwner
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

    /**
     * List of child [ViewModelStoreOwner]s, which `this` fragment can access.
     * You should manually add them. List will be cleared in [onDestroy].
     */
    protected val childViewModelOwners = arrayListOf<ViewModelStoreOwner>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSoftInputMode // initialize
        updateSoftInputMode()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragments()
        setUp()
        setViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        restoreSoftInputMode()
        childViewModelOwners.clear()
    }

    private fun updateSoftInputMode() {
        getSoftInputMode()?.let {
            activity?.window?.setSoftInputMode(it)
        }
    }

    private fun restoreSoftInputMode() {
        initialSoftInputMode?.let {
            activity?.window?.setSoftInputMode(it)
        }
    }

    /**
     * Specifies windoSoftInputMode for `this` fragment.
     */
    protected open fun getSoftInputMode(): Int? =
        null

    /**
     * Configures child fragments.
     * Being called in [Fragment.onViewCreated] method.
     */
    protected open fun setFragments() {
        // default empty implementation
    }

    /**
     * Configures non-view components.
     * Being called in [Fragment.onViewCreated] method.
     */
    @CallSuper
    protected open fun setUp() {
        collectViewModelsData()
    }

    /**
     * Collects (subscribes to) data from declared ViewModel's.
     * Being called in [setUp] method.
     */
    protected abstract fun collectViewModelsData()

    /**
     * Sets fragment's views and configures them.
     * Being called in [Fragment.onViewCreated] method.
     */
    protected abstract fun setViews()

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