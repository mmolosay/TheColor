package com.ordolabs.feature_home.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ordolabs.feature_home.ui.fragment.home.HomeFragment
import com.ordolabs.thecolor.model.color.ColorPreview
import com.ordolabs.thecolor.viewmodel.factory.AssistedSavedStateViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class HomeViewModel @AssistedInject constructor(
    @Assisted private val stateHandle: SavedStateHandle
) : ViewModel() {

    var isColorDataShown: Boolean
        get() = stateHandle.get<Boolean>(KEY_IS_COLOR_DATA_SHOWN) ?: false
        set(value) = stateHandle.set(KEY_IS_COLOR_DATA_SHOWN, value)

    var preview: ColorPreview?
        get() = stateHandle.get<ColorPreview>(KEY_PREVIEW)
        set(value) = stateHandle.set(KEY_PREVIEW, value)

    var state: HomeFragment.State
        get() = stateHandle.get<HomeFragment.State>(KEY_STATE) ?: HomeFragment.State.BLANK
        set(value) = stateHandle.set(KEY_STATE, value)

    @AssistedFactory
    interface Factory : AssistedSavedStateViewModelFactory<HomeViewModel>

    companion object {

        private const val KEY_IS_COLOR_DATA_SHOWN = "KEY_IS_COLOR_DATA_SHOWN"
        private const val KEY_PREVIEW = "KEY_PREVIEW"
        private const val KEY_STATE = "KEY_STATE"
    }
}