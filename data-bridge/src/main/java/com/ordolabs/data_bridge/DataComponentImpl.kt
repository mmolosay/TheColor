package com.ordolabs.data_bridge

import com.ordolabs.core.di.data.DataComponent
import com.ordolabs.data_bridge.model.DataModuleImpl
import dagger.Component

@Component(
    modules = [DataModuleImpl::class]
)
interface DataComponentImpl : DataComponent {

    @Component.Builder
    interface Builder {

        fun build(): DataComponentImpl
    }
}