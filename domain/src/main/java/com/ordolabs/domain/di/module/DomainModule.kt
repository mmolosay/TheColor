package com.ordolabs.domain.di.module

import dagger.Module

@Module(
    includes = [
        DomainUseCaseModule::class
    ]
)
interface DomainModule