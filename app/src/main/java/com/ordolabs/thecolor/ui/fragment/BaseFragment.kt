package com.ordolabs.thecolor.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ordolabs.thecolor.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

abstract class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes) {

    val transactionTag: String = this::class.java.simpleName

    @IdRes
    open val defaultFragmentContainerId: Int = R.id.defaultFragmentContainer

    /**
     * Initial soft input mode, which `activity` had before adding `this` fragment.
     */
    protected val initialSoftInputMode: Int? by lazy {
        activity?.window?.attributes?.softInputMode
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSoftInputMode // initialize
        setSoftInputMode()
        parseStartIntent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parseStartIntent()
        setUp()
        setViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        initialSoftInputMode?.let { activity?.window?.setSoftInputMode(it) }
    }

    protected open fun setSoftInputMode() {
        // override me
    }

    /**
     * Parses `Intent`, that started this `Fragment`.
     * Being called in [Fragment.onCreate] method.
     */
    protected open fun parseStartIntent() {
        // override me
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
            lifecycle.repeatOnLifecycle(state) {
                this@collectOnLifecycle.collect(action)
            }
        }
}