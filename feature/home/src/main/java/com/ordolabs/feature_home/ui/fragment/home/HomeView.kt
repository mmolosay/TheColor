package com.ordolabs.feature_home.ui.fragment.home

/**
 * At the moment, empty interface and just place for [HomeView.State].
 */
interface HomeView {

    enum class State {
        /**
         * Has neither color preview or color data shown.
         */
        BLANK {
            override fun isBlank(): Boolean = true
            override fun isPreview(): Boolean = false
            override fun isData(): Boolean = false
        },

        /**
         * Has color preview shown only.
         */
        PREVIEW {
            override fun isBlank(): Boolean = false
            override fun isPreview(): Boolean = true
            override fun isData(): Boolean = false
        },

        /**
         * Has both color preview and color data shown.
         */
        DATA {
            override fun isBlank(): Boolean = false
            override fun isPreview(): Boolean = false
            override fun isData(): Boolean = true
        };

        // utils to make checking specific state easier
        abstract fun isBlank(): Boolean
        abstract fun isPreview(): Boolean
        abstract fun isData(): Boolean
    }
}