package com.ordolabs.feature_home.ui.fragment.color.input

import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.colorinput.ColorInputViewModel
import com.ordolabs.feature_home.viewmodel.colorinput.ColorValidatorViewModel
import com.ordolabs.thecolor.model.color.ColorPrototype
import com.ordolabs.thecolor.util.struct.Resource
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * Base `Fragment`, that can obtain input from UI and [assembleColor] of type [C].
 *
 * Derived class should call [validateOnInputChanges] every time data in UI input(s) was changed.
 *
 * All derived classes are designed to work together simultaneously (for example, in `ViewPager`),
 * thus if color changes in any of them, changes should be reflected in all others.
 *
 * Color collected from [getColorInputFlow] will be used to [populateViews] and [clearViews].
 *
 * @see ColorInputHexFragment
 * @see ColorInputRgbFragment
 */
// TODO: should not validate color, but pass collected prototype to parent
abstract class BaseColorInputFragment<C : ColorPrototype> : BaseFragment {

    constructor() : super()
    constructor(@LayoutRes layoutRes: Int) : super(layoutRes)

    protected val colorInputVM: ColorInputViewModel by sharedViewModel()

    private val colorValidatorVM: ColorValidatorViewModel by sharedViewModel()
    private var latestColor: C? = null
    private var isTypedByUser: Boolean = true

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
     * Returns flow to be collected from [colorInputVM].
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
     * Performs [assembleColor] and sends acquired color on validation,
     * if data in UI input(s) was changed by user.
     *
     * Must be called in UI input(s) observers of derived class.
     */
    protected fun validateOnInputChanges() {
        if (!isTypedByUser) return
        val color = assembleAndMementoColor()
        colorValidatorVM.validateColor(color)
    }

    private fun assembleAndMementoColor(): C =
        assembleColor().also { color ->
            this.latestColor = color
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
}