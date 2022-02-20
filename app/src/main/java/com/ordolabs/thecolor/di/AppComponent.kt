package com.ordolabs.thecolor.di

import com.ordolabs.domain.di.DomainComponent
import com.ordolabs.thecolor.di.module.AppModule
import com.ordolabs.thecolor.di.scope.AppScope
import dagger.Component

@AppScope
@Component(
    modules = [AppModule::class],
    dependencies = [DomainComponent::class]
)
interface AppComponent : AppProvisions {

    @Component.Builder
    interface Builder {
        fun domainComponent(instance: DomainComponent): Builder
        fun build(): AppComponent
    }
}