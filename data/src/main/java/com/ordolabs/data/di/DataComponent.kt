package com.ordolabs.data.di

import com.ordolabs.data.di.model.DataModule
import dagger.Component

@Component(
    modules = [DataModule::class]
)
interface DataComponent {

    @Component.Builder
    interface Builder {

        fun build(): DataComponent
    }
}