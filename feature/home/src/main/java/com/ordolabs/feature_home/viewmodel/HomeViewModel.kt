package com.ordolabs.feature_home.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ordolabs.feature_home.ui.fragment.home.HomeView
import com.ordolabs.thecolor.model.color.ColorPreview
import com.ordolabs.thecolor.viewmodel.factory.AssistedSavedStateViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class HomeViewModel @AssistedInject constructor(
    @Assisted private val stateHandle: SavedStateHandle
) : ViewModel() {

    var preview: ColorPreview?
        get() = stateHandle.get<ColorPreview>(KEY_PREVIEW)
        set(value) = stateHandle.set(KEY_PREVIEW, value)

    var stateType: HomeView.State.Type
        get() = stateHandle.get<HomeView.State.Type>(KEY_STATE) ?: HomeView.State.Type.BLANK
        set(value) = stateHandle.set(KEY_STATE, value)

    @AssistedFactory
    interface Factory : AssistedSavedStateViewModelFactory<HomeViewModel>

    companion object {

        private const val KEY_PREVIEW = "KEY_PREVIEW"
        private const val KEY_STATE = "KEY_STATE"
    }
}