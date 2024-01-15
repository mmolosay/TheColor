package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.base

/**
 * Interface for `View` that populates data [D] in its UI.
 */
interface ColorDataView<D> {

    fun populateViews(data: D)
}