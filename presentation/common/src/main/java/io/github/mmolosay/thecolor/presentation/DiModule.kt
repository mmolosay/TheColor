package io.github.mmolosay.thecolor.presentation

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.migration.DisableInstallInCheck

@Module(
    includes = [DiModule.ProvideModule::class, DiModule.BindModule::class],
)
@InstallIn(SingletonComponent::class)
object DiModule {

    @Module
    @DisableInstallInCheck
    object ProvideModule

    @Module
    @DisableInstallInCheck
    interface BindModule
}