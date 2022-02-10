package com.ordolabs.thecolor.di.module

import dagger.Module

@Module(
    includes = [
        AppViewModelModule::class
    ]
)
interface AppModule