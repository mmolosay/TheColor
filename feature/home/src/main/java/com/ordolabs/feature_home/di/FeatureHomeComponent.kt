package com.ordolabs.feature_home.di

import com.ordolabs.feature_home.di.module.FeatureHomeModule
import com.ordolabs.thecolor.di.scope.FeatureScope
import dagger.Component

@FeatureScope
@Component(
    modules = [FeatureHomeModule::class]
)
interface FeatureHomeComponent