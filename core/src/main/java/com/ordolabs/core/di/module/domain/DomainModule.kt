package com.ordolabs.di.module.domain

import dagger.Module

@Module(
    includes = [
        DomainUseCaseModule::class
    ]
)
interface DomainModule