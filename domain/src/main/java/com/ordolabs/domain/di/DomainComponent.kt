package com.ordolabs.domain.di

import com.ordolabs.domain.di.module.DomainModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [DomainModule::class],
    dependencies = [RepositoryProvisions::class]
)
interface DomainComponent : DomainProvisions {

    @Component.Builder
    interface Builder {

        fun repositoryProvisions(instance: RepositoryProvisions): Builder
        fun build(): DomainComponent
    }
}