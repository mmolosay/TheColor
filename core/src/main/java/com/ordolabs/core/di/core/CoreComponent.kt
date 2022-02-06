package com.ordolabs.core.di.core

import com.ordolabs.core.di.data.DataComponent
import dagger.Component

@Component(
    modules = [CoreModule::class],
    dependencies = [DataComponent::class]
)
interface CoreComponent {

    @Component.Builder
    interface Builder {

        fun dataComponent(c: DataComponent)

        fun build(): CoreComponent
    }
}