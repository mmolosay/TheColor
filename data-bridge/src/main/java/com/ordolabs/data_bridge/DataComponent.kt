package com.ordolabs.data_bridge

import com.ordolabs.data_bridge.model.DataModule
import dagger.Subcomponent

@Subcomponent(
    modules = [DataModule::class]
)
interface DataComponent {

    @Subcomponent.Builder
    interface Builder {

        fun build(): DataComponent
    }
}