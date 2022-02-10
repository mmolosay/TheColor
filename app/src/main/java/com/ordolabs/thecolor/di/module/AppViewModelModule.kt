package com.ordolabs.thecolor.di.module

import androidx.lifecycle.ViewModelProvider
import com.ordolabs.thecolor.viewmodel.factory.ViewModelFactory
import dagger.Binds

interface AppViewModelModule {

    @Binds
    fun bindViewModelFactory(instance: ViewModelFactory): ViewModelProvider.Factory
}