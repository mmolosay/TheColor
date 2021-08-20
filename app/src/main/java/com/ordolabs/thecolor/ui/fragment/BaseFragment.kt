package com.ordolabs.thecolor.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes) {

    val transactionTag: String = this::class.java.simpleName

    protected val initialSoftInputMode: Int? by lazy {
        activity?.window?.attributes?.softInputMode
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSoftInputMode // initialize
        setSoftInputMode()
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
     * Parses `Intent`, that started this `Activity`.
     */
    protected open fun parseStartIntent() {
        // override me
    }

    /**
     * Configures non-view components.
     */
    abstract fun setUp()

    /**
     * Sets activity's views and configures them.
     */
    abstract fun setViews()

    companion object {
        // extra keys and stuff
    }
}