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

    // region Provisions

    // provisions are declared in-place (not in interface),
    // because feature components are final and should not be inherited

    val viewModelFactory: ViewModelProvider.Factory

    // endregion

    // region Injections


    fun inject(dest: HomeFragment)

    // endregion

    @Component.Builder
    interface Builder {
        fun appComponent(appComponent: AppComponent): Builder
        fun build(): FeatureHomeComponent
    }
}