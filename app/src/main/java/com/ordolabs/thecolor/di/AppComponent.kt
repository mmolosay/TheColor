package com.ordolabs.thecolor.di

import android.content.Context
import com.ordolabs.data_bridge.DataComponent
import dagger.BindsInstance
import dagger.Component

@AppScope
@Component(
    modules = [AppModule::class],
    dependencies = [DataComponent::class]
)
interface AppComponent {

    @Component.Builder
    interface Builder {

        fun dataComponent(dataComponent: DataComponent): Builder

        @BindsInstance
        fun dependencies(deps: Dependencies): Builder

        fun build(): AppComponent
    }

    interface Dependencies {

        val applicationContext: Context
    }
}