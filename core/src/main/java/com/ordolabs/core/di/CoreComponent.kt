package com.ordolabs.core.di

import dagger.Component
import javax.inject.Singleton

@Component
@Singleton
class CoreComponent {

    @Component.Builder
    interface Builder {

        fun build(): CoreComponent
    }
}