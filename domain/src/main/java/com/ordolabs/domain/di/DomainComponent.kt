package com.ordolabs.domain.di

import com.ordolabs.domain.di.module.DomainModule
import dagger.Component

@Component(modules = [DomainModule::class])
interface DomainComponent : DomainProvisions {

    @Component.Builder
    interface Builder {

        fun build(): DomainComponent
    }
}