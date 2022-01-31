package com.ordolabs.feature_home.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class HomeViewModel(
    private val state: SavedStateHandle
) : ViewModel() {

    var isColorDataShown: Boolean
        get() = state.get<Boolean>(KEY_IS_COLOR_DATA_SHOWN) ?: false
        set(value) = state.set(KEY_IS_COLOR_DATA_SHOWN, value)

    companion object {

        private const val KEY_IS_COLOR_DATA_SHOWN = "KEY_IS_COLOR_DATA_SHOWN"
    }
}