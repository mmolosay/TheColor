package com.ordolabs.feature_home.ui.fragment.home

import io.github.mmolosay.presentation.model.color.Color
import io.github.mmolosay.presentation.model.color.ColorPreview

/**
 * Interface for home `View`, which is finite-state machine: see [HomeView.State].
 */
interface HomeView {

    var state: State

    fun changeState(type: State.Type)

    sealed class State(
        protected val view: HomeView
    ) {
        /**
         * Restores UI for current state.
         */
        abstract fun restoreState()

        /**
         * Sets UI in [Type.BLANK] state.
         */
        abstract fun showBlank()

        /**
         * Sets UI in [Type.PREVIEW] state.
         */
        abstract fun showPreview(preview: ColorPreview)

        /**
         * Sets UI in [Type.DATA] state.
         */
        abstract fun showData(color: Color)

        enum class Type {
            /**
             * Has neither color preview or color data shown.
             */
            BLANK,

            /**
             * Has color preview shown only.
             */
            PREVIEW,

            /**
             * Has both color preview and color data shown.
             */
            DATA
        }
    }
}