package com.ordolabs.thecolor.ui.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ordolabs.thecolor.util.ext.getDefaultTransactionTag

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            parseArguments(it)
        }
        setUp()
        setFragmentResultListeners()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragments()
        setViews()
    }

    // region Fragment.onCreate

    /**
     * Parses [getArguments].
     * Being called in [Fragment.onCreate] method.
     */
    protected open fun parseArguments(args: Bundle) {
        // default empty implementation
    }

    /**
     * Configures non-view components.
     * Being called in [Fragment.onCreate] method.
     */
    protected open fun setUp() {
        // default empty implementation
    }

    /**
     * Sets listeners for Fragment Result API.
     * Being called in [Fragment.onCreate] method.
     */
    protected open fun setFragmentResultListeners() {
        // default empty implementation
    }

    // endregion

    // region Fragment.onViewCreated

    /**
     * Configures child fragments.
     * Being called in [Fragment.onViewCreated] method.
     */
    protected open fun setFragments() {
        // default empty implementation
    }

    /**
     * Sets `Fragment's views and configures them.
     * Being called in [Fragment.onViewCreated] method.
     */
    protected abstract fun setViews()

    // endregion

    fun show(manager: FragmentManager) {
        val tag = this.getDefaultTransactionTag()
        super.show(manager, tag)
    }
}