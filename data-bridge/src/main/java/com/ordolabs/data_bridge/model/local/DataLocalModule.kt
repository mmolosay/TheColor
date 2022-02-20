package com.ordolabs.data_bridge.model.local

import dagger.Module

@Module(
    includes = [
        DataLocalDatabaseModule::class,
        DataLocalPreferencesModule::class
    ]
)
interface DataLocalModule