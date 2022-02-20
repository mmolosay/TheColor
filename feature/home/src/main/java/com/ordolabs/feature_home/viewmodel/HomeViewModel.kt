package com.ordolabs.feature_home.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ordolabs.thecolor.viewmodel.factory.AssistedSavedStateViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class HomeViewModel @AssistedInject constructor(
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    var isColorDataShown: Boolean
        get() = state.get<Boolean>(KEY_IS_COLOR_DATA_SHOWN) ?: false
        set(value) = state.set(KEY_IS_COLOR_DATA_SHOWN, value)

    @AssistedFactory
    interface Factory : AssistedSavedStateViewModelFactory<HomeViewModel> {
        // TODO: try to remove empty override
        override fun create(savedStateHandle: SavedStateHandle): HomeViewModel
    }

    companion object {

        private const val KEY_IS_COLOR_DATA_SHOWN = "KEY_IS_COLOR_DATA_SHOWN"
    }
}