package com.ordolabs.core.di

import com.ordolabs.data_bridge.DataComponent
import com.ordolabs.domain.di.DomainComponent
import dagger.Module

@Module(
    subcomponents = [
        DataComponent::class,
        DomainComponent::class
    ]
)
interface CoreModule