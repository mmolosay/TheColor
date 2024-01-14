package io.github.mmolosay.main

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * DI module for components of __data__ architectural layer.
 */
@Module(
    includes = [DiDataRemoteModule::class, DiDataLocalModule::class],
)
@InstallIn(SingletonComponent::class)
object DiDataModule