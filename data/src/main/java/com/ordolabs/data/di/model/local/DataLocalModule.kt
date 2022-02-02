package com.ordolabs.data.di.model.local

import dagger.Module

@Module(
    includes = [
        DataLocalDatabaseModule::class,
        DataLocalPreferencesModule::class
    ]
)
interface DataLocalModule