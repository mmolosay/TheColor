package com.ordolabs.di.module.data

import com.ordolabs.di.module.data.local.DataLocalModule
import com.ordolabs.di.module.data.remote.DataRemoteModule
import com.ordolabs.di.module.data.repository.DataRepositoryModule
import dagger.Module

@Module(
    includes = [
        DataRepositoryModule::class,
        DataLocalModule::class,
        DataRemoteModule::class
    ]
)
interface DataModule