package com.ordolabs.feature_home.di

import androidx.lifecycle.ViewModelProvider
import com.ordolabs.feature_home.di.module.FeatureHomeModule
import com.ordolabs.feature_home.ui.fragment.HomeFragment
import com.ordolabs.thecolor.di.AppComponent
import com.ordolabs.thecolor.di.scope.FeatureScope
import dagger.Component

@FeatureScope
@Component(
    modules = [FeatureHomeModule::class],
    dependencies = [AppComponent::class]
)
interface FeatureHomeComponent {

    val viewModelFactory: ViewModelProvider.Factory

    fun inject(dest: HomeFragment)

    @Component.Builder
    interface Builder {
        fun appComponent(appComponent: AppComponent): Builder
        fun build(): FeatureHomeComponent
    }
}