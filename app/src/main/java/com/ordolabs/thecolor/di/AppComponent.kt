package com.ordolabs.thecolor.di

import com.ordolabs.core.di.CoreComponent
import com.ordolabs.thecolor.di.module.AppModule
import com.ordolabs.thecolor.di.scope.AppScope
import dagger.Component

@AppScope
@Component(
    modules = [AppModule::class],
    dependencies = [CoreComponent::class]
)
interface AppComponent {

    @Component.Builder
    interface Builder {

        fun coreComponent(instance: CoreComponent): Builder

        fun build(): AppComponent
    }
}