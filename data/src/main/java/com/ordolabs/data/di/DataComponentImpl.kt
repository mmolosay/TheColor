package com.ordolabs.data.di

import com.ordolabs.core.di.data.DataComponent
import com.ordolabs.data.di.model.DataModuleImpl
import dagger.Component

@Component(
    modules = [DataModuleImpl::class]
)
internal interface DataComponentImpl : DataComponent {

    @Component.Builder
    interface Builder {

        fun build(): DataComponentImpl
    }
}