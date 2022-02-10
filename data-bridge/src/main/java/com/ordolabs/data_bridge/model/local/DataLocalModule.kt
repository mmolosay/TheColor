package com.ordolabs.data_bridge.model.local

import com.ordolabs.data_bridge.model.local.database.DataLocalDatabaseModule
import com.ordolabs.data_bridge.model.local.preferences.DataLocalPreferencesModule
import dagger.Module

@Module(
    includes = [
        DataLocalDatabaseModule::class,
        DataLocalPreferencesModule::class
    ]
)
interface DataLocalModule