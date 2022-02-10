package com.ordolabs.domain.di

import com.ordolabs.domain.di.module.DomainModule
import dagger.Subcomponent

@Subcomponent(modules = [DomainModule::class])
interface DomainComponent {

    @Subcomponent.Builder
    interface Builder {

        fun build(): DomainComponent
    }
}