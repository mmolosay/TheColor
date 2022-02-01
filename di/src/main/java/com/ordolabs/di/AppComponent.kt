package com.ordolabs.di

import android.content.Context
import com.ordolabs.di.module.app.AppModule
import com.ordolabs.di.module.data.DataModule
import com.ordolabs.di.module.domain.DomainModule
import dagger.BindsInstance
import dagger.Component

@Component(
    modules = [
        AppModule::class,
        DomainModule::class,
        DataModule::class
    ]
)
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun dependencies(deps: Dependencies): Builder

        fun build(): AppComponent
    }

    interface Dependencies {

        val applicationContext: Context
    }
}