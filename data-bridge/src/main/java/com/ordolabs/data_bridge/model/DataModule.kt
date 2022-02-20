package com.ordolabs.data_bridge.model

import com.ordolabs.data_bridge.model.local.DataLocalModule
import com.ordolabs.data_bridge.model.remote.DataRemoteModule
import com.ordolabs.data_bridge.model.repository.DataRepositoryModule
import dagger.Module

@Module(
    includes = [
        DataLocalModule::class,
        DataRemoteModule::class,
        DataRepositoryModule::class
    ]
)
interface DataModule