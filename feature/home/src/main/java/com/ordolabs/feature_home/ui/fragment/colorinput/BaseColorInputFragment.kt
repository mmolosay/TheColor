package com.ordolabs.feature_home.ui.fragment.colorinput

import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.colorinput.ColorInputViewModel
import com.ordolabs.thecolor.model.color.AbstractColor
import com.ordolabs.thecolor.util.struct.Resource
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * Base Fragment, that can obtain input from UI and [assembleColor] of type [C].
 * Color should be assembled and validated every time input changes.
 * Color collected from [getColorInputFlow] will be used to [populateViews] and [clearViews].
 *
 * @see validateColorInput
 */
abstract class BaseColorInputFragment<C : AbstractColor> : BaseFragment {

    constructor() : super()
    constructor(@LayoutRes layoutRes: Int) : super(layoutRes)

    protected var isTypedByUser: Boolean = true

    private val colorInputVM: ColorInputViewModel by sharedViewModel()
    private var latestColor: C? = null

    // region Abstract

    /**
     * Collects user input from UI and makes new instance of [C] out of it.
     */
    protected abstract fun assembleColor(): C

    /**
     * Populates Fragment's input views with data from [color].
     */
    protected abstract fun populateViews(color: C)

    /**
     * Clears all views from input.
     */
    protected abstract fun clearViews()

    /**
     * Returns flow to be collected.
     */
    protected abstract fun getColorInputFlow(): Flow<Resource<C>>

    // endregion

    @CallSuper
    override fun collectViewModelsData() {
        collectColorInput()
    }

    private fun collectColorInput() =
        getColorInputFlow().collectOnLifecycle { resource ->
        resource.fold(
            onEmpty = ::onColorInputEmpty,
            onSuccess = ::onColorInputSuccess
        )
    }

    /**
     * Validates color, provided by [assembleColor].
     * Should be called by derived class when input changes.
     */
    protected fun validateColorInput() {
        val color = assembleAndMementoColor()
        colorInputVM.validateColor(color)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onColorInputEmpty(previous: C?) {
        if (isResumed) return // prevent user interrupting
        this.isTypedByUser = false
        clearViews()
        this.isTypedByUser = true
    }

    private fun onColorInputSuccess(color: C) {
        if (isResumed && color == latestColor) return // prevent user interrupting
        this.isTypedByUser = false
        populateViews(color)
        this.isTypedByUser = true
    }

    private fun assembleAndMementoColor(): C =
        assembleColor().also { color ->
            this.latestColor = color
        }
}