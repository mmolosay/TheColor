package com.ordolabs.di

import android.app.Application
import com.ordolabs.di.module.app.AppModule
import com.ordolabs.di.module.data.DataModule
import com.ordolabs.di.module.domain.DomainModule
import dagger.Component

// TODO: rename in "MasterComponent"?
@Component(modules = [
    AppModule::class,
    DomainModule::class,
    DataModule::class
])
interface AppComponent {

    @Component.Builder
    interface Builder {
        fun setApplication(app: Application): Builder
        fun build(): AppComponent
    }
}