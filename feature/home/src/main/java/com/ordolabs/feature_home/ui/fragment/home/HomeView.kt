package com.ordolabs.feature_home.ui.fragment.home

/**
 * At the moment, empty interface and just place for [HomeView.State].
 */
interface HomeView {

    enum class State {
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