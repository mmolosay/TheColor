package com.ordolabs.feature_home.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ordolabs.thecolor.model.color.Color
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

    var color: Color?
        get() = state.get<Color>(KEY_COLOR)
        set(value) = state.set(KEY_COLOR, value)

    @AssistedFactory
    interface Factory : AssistedSavedStateViewModelFactory<HomeViewModel>

    companion object {

        private const val KEY_IS_COLOR_DATA_SHOWN = "KEY_IS_COLOR_DATA_SHOWN"
        private const val KEY_COLOR = "KEY_COLOR"
    }
}