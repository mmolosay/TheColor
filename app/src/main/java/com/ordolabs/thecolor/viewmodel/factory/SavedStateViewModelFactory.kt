package com.ordolabs.thecolor.viewmodel.factory

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import javax.inject.Inject

/**
 * Factory of factories of [ViewModel]s with [SavedStateHandle] to be assisted-injected.
 */
class SavedStateViewModelFactory @Inject constructor(
    private val factories: Map<Class<out ViewModel>, @JvmSuppressWildcards AssistedSavedStateViewModelFactory<out ViewModel>>
) {

    fun create(
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle? = null
    ) =
        object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

            @Suppress("UNCHECKED_CAST")
            override fun <VM : ViewModel?> create(
                key: String,
                modelClass: Class<VM>,
                handle: SavedStateHandle
            ): VM {
                val factoryKey = modelClass as Class<ViewModel>
                val factory = factories.getValue(factoryKey)
                return factory.create(handle) as VM
            }
        }
}