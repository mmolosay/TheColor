package com.ordolabs.feature_home.di.module

import dagger.Module

@Module(
    includes = [
        FeatureHomeViewModelModule::class
    ]
)
interface FeatureHomeModule