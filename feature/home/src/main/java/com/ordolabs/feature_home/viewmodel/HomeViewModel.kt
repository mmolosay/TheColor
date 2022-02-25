package com.ordolabs.feature_home.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ordolabs.thecolor.model.color.ColorPreview
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

    var preview: ColorPreview?
        get() = state.get<ColorPreview>(KEY_PREVIEW)
        set(value) = state.set(KEY_PREVIEW, value)

    @AssistedFactory
    interface Factory : AssistedSavedStateViewModelFactory<HomeViewModel>

    companion object {

        private const val KEY_IS_COLOR_DATA_SHOWN = "KEY_IS_COLOR_DATA_SHOWN"
        private const val KEY_PREVIEW = "KEY_PREVIEW"
    }
}