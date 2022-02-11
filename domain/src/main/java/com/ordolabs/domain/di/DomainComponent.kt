package com.ordolabs.domain.di

import com.ordolabs.domain.di.module.DomainModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [DomainModule::class],
    dependencies = [DomainDependencies::class]
)
interface DomainComponent : DomainProvisions {

    @Component.Builder
    interface Builder {

        fun dependencies(instance: DomainDependencies): Builder
        fun build(): DomainComponent
    }
}