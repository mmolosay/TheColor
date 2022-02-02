package com.ordolabs.core.di

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [CoreModule::class])
class CoreComponent {

    @Component.Builder
    interface Builder {

        fun build(): CoreComponent
    }
}