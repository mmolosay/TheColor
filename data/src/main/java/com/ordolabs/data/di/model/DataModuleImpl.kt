package com.ordolabs.data.di.model

import com.ordolabs.core.di.data.DataModule
import com.ordolabs.data.di.model.local.DataLocalModule
import com.ordolabs.data.di.model.remote.DataRemoteModule
import com.ordolabs.data.di.model.repository.DataRepositoryModule
import dagger.Module

@Module(
    includes = [
        DataLocalModule::class,
        DataRemoteModule::class,
        DataRepositoryModule::class
    ]
)
interface DataModuleImpl : DataModule