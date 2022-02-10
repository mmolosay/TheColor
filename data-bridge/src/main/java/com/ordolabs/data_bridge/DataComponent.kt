package com.ordolabs.data_bridge

import com.ordolabs.data_bridge.model.DataModule
import dagger.Component

@Component(modules = [DataModule::class])
interface DataComponent : DataProvisions {

    @Component.Builder
    interface Builder {

        fun build(): DataComponent
    }
}