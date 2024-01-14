package com.ordolabs.feature_home.ui.fragment.color.input.page

import androidx.annotation.CallSuper
import com.ordolabs.feature_home.viewmodel.color.input.ColorInputViewModel
import com.ordolabs.util.Resource
import io.github.mmolosay.presentation.fragment.BaseFragment
import io.github.mmolosay.presentation.model.color.ColorPrototype
import io.github.mmolosay.presentation.util.ext.parentViewModels
import io.github.mmolosay.presentation.util.ext.requireParentOf
import kotlinx.coroutines.flow.Flow

/**
 * Base `Fragment`, that can obtain input from UI and [assemblePrototype] of type [C].
 * Requires parent `Fragment` to be an instance of [ColorInputParent].
 *
 * Derived class should call [outputOnInputChanges] every time data in UI input(s) was changed.
 *
 * All derived classes are designed to work together simultaneously (for example, in `ViewPager`),
 * thus if color changes in any of them, changes should be reflected in all others.
 * All derived classes share common `ViewModel`, scoped to parent `Fragment`.
 *
 * Color collected from [getColorInputFlow] will be used to [populateViews] and [clearViews].
 *
 * @see ColorInputHexFragment
 * @see ColorInputRgbFragment
 */
abstract class BaseColorInputFragment<C : ColorPrototype> : BaseFragment() {

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
    protected abstract fun getColorInputFlow(): Flow<Resource<C>>

    // endregion

    protected val colorInputVM: ColorInputViewModel by parentViewModels()

    private val parent: ColorInputParent? by lazy { requireParentOf() }
    private var currentPrototype: ColorPrototype? = null
    private var isTypedByUser: Boolean = true

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
     * Performs [assemblePrototype] and updates [parent] with it.
     *
     * Must be called in UI input(s) observers of derived class.
     */
    protected fun outputOnInputChanges() {
        if (!isTypedByUser) return
        val prototype = assembleAndMementoPrototype()
        parent?.onInputChanged(prototype)
    }

    private fun assembleAndMementoPrototype() =
        assemblePrototype().also { prototype ->
            this.currentPrototype = prototype
        }

    private fun updateInputs(block: () -> Unit) {
        this.isTypedByUser = false
        block()
        this.isTypedByUser = true
    }

    private fun onColorInputEmpty() {
        if (isResumed) return // prevent user interrupting
        updateInputs {
            clearViews()
        }
        this.currentPrototype = null
    }

    private fun onColorInputSuccess(input: C) {
        if (input == currentPrototype) return // desired color already set
        updateInputs {
            populateViews(input)
        }
        this.currentPrototype = input
    }
}