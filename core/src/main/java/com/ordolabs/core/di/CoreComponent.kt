package com.ordolabs.core.di

import android.content.Context
import com.ordolabs.data_bridge.DataComponent
import com.ordolabs.domain.di.DomainComponent
import dagger.BindsInstance
import dagger.Component

@Component(modules = [CoreModule::class])
interface CoreComponent {

    fun dataComponent(): DataComponent.Builder
    fun domainComponent(): DomainComponent.Builder

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun applicationContext(instance: Context): Builder

        fun build(): CoreComponent
    }
}