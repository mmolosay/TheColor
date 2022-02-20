package com.ordolabs.data_bridge

import android.content.Context
import com.ordolabs.data_bridge.model.DataModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DataModule::class])
interface DataComponent : DataProvisions {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun applicationContext(instance: Context): Builder
        fun build(): DataComponent
    }
}