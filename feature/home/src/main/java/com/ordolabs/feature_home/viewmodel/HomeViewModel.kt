package com.ordolabs.feature_home.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class HomeViewModel(
    private val state: SavedStateHandle
) : ViewModel() {

    var isInfoSheetShown: Boolean
        get() = state.get<Boolean>(KEY_IS_INFO_SHEET_SHOWN) ?: false
        set(value) = state.set(KEY_IS_INFO_SHEET_SHOWN, value)

    companion object {

        private const val KEY_IS_INFO_SHEET_SHOWN = "KEY_IS_INFO_SHEET_SHOWN"
    }
}