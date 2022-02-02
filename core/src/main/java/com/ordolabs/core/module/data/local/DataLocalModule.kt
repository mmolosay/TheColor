package com.ordolabs.di.module.data.local

import dagger.Module

@Module(
    includes = [
        DataLocalDatabaseModule::class,
        DataLocalPreferencesModule::class
    ]
)
interface DataLocalModule