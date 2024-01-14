package io.github.mmolosay.main

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * DI module for components of __domain__ architectural layer.
 */
@Module
@InstallIn(SingletonComponent::class)
object DiDomainModule {
}