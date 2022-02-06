package com.ordolabs.thecolor.di

import android.content.Context
import com.ordolabs.core.di.CoreComponent
import dagger.Component

@AppScope
@Component(
    modules = [AppModule::class],
    dependencies = [CoreComponent::class]
)
interface AppComponent {

//    @Component.Builder
//    interface Builder {
//
////        @BindsInstance
////        fun dependencies(deps: Dependencies): Builder
//
//        fun build(): AppComponent
//    }

    interface Dependencies {

        val applicationContext: Context
    }
}