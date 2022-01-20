package com.ordolabs.feature_home.ui.fragment.color.input.page

import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.colorinput.ColorInputViewModel
import com.ordolabs.thecolor.model.color.ColorInput
import com.ordolabs.thecolor.model.color.ColorPrototype
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.getOrNull
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * Base `Fragment`, that can obtain input from UI and [assemblePrototype] of type [C].
 *
 * Derived class should call [outputOnInputChanges] every time data in UI input(s) was changed.
 *
 * All derived classes are designed to work together simultaneously (for example, in `ViewPager`),
 * thus if color changes in any of them, changes should be reflected in all others.
 *
 * Color collected from [getColorInputFlow] will be used to [populateViews] and [clearViews].
 *
 * @see ColorInputHexFragment
 * @see ColorInputRgbFragment
 */
abstract class BaseColorInputFragment<C : ColorPrototype> : BaseFragment {

    constructor() : super()
    constructor(@LayoutRes layoutRes: Int) : super(layoutRes)

    protected val colorInputVM: ColorInputViewModel by sharedViewModel()

    private var isTypedByUser: Boolean = true

    // region Abstract

    /**
     * Collects user input from UI and makes new instance of [ColorPrototype] out of it.
     */
    protected abstract fun assemblePrototype(): ColorPrototype

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
    protected abstract fun getColorInputFlow(): Flow<Resource<ColorInput<C>>>

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
     * Performs [assemblePrototype] and updates [ColorInputViewModel] with it.
     *
     * Must be called in UI input(s) observers of derived class.
     */
    protected fun outputOnInputChanges() {
        if (!isTypedByUser) return
        val prototype = assemblePrototype()
        colorInputVM.updateColorPrototype(prototype)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onColorInputEmpty(previous: ColorInput<C>?) {
        if (isResumed) return // prevent user interrupting
        this.isTypedByUser = false
        clearViews()
        this.isTypedByUser = true
        colorInputVM.clearColorPrototype()
    }

    private fun onColorInputSuccess(input: ColorInput<C>) {
        val new = input.color
        val current = colorInputVM.prototype.value.getOrNull()
        if (new == current) return // desired color already set
        if (input.forcePopulate || (!isResumed && !input.forcePopulate)) {
            this.isTypedByUser = false
            populateViews(new)
            this.isTypedByUser = true
            colorInputVM.updateColorPrototype(new)
        }
    }
}