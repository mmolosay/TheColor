package com.ordolabs.thecolor.viewmodel.factory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

/**
 * Factory for creating [ViewModel]s with `SavedStateHandle` as assisted-inject parameter in constructor.
 */
interface AssistedSavedStateViewModelFactory<VM : ViewModel> {
    fun create(savedStateHandle: SavedStateHandle): VM
}