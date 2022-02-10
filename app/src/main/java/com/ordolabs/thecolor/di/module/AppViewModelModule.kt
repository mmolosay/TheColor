package com.ordolabs.thecolor.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ordolabs.thecolor.viewmodel.factory.ViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.Multibinds

@Module
interface AppViewModelModule {

    @Multibinds
    fun multibindViewModels(): Map<Class<out ViewModel>, @JvmSuppressWildcards ViewModel>

    @Binds
    fun bindViewModelFactory(instance: ViewModelFactory): ViewModelProvider.Factory
}