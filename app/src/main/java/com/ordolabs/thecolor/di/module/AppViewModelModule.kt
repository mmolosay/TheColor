package com.ordolabs.thecolor.di.module

import androidx.lifecycle.ViewModelProvider
import com.ordolabs.thecolor.viewmodel.factory.ViewModelFactory
import dagger.Binds
import dagger.Module

@Module
interface AppViewModelModule {

    // TODO: implement or remove
//    @Multibinds
//    fun mapViewModels(): Map<>

    @Binds
    fun bindViewModelFactory(instance: ViewModelFactory): ViewModelProvider.Factory
}