package com.ordolabs.thecolor.di

import com.ordolabs.data_bridge.DataComponent
import com.ordolabs.domain.di.DomainComponent
import com.ordolabs.thecolor.di.module.AppModule
import com.ordolabs.thecolor.di.scope.AppScope
import dagger.Component

@AppScope
@Component(
    modules = [AppModule::class],
    dependencies = [
        DataComponent::class,
        DomainComponent::class
    ]
)
interface AppComponent {

//    val viewModelMultibinding: Map<Class<out ViewModel>, @JvmSuppressWildcards ViewModel>
//    val viewModelFactory: ViewModelProvider.Factory

    @Component.Builder
    interface Builder {

        fun dataComponent(instance: DataComponent): Builder
        fun domainComponent(instance: DomainComponent): Builder

        fun build(): AppComponent
    }
}