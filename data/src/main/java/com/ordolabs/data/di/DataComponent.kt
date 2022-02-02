package com.ordolabs.data.di

import com.ordolabs.data.di.model.local.DataLocalModule
import com.ordolabs.data.di.model.remote.DataRemoteModule
import com.ordolabs.data.di.model.repository.DataRepositoryModule
import dagger.Component

@Component(
    modules = [
        DataLocalModule::class,
        DataRemoteModule::class,
        DataRepositoryModule::class
    ]
)
class DataComponent {

    @Component.Builder
    interface Builder {

        fun build(): DataComponent
    }
}