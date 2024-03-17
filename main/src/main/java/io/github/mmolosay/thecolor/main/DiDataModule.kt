package io.github.mmolosay.thecolor.main

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.migration.DisableInstallInCheck
import io.github.mmolosay.thecolor.data.GetColorLightnessUseCaseImpl
import io.github.mmolosay.thecolor.domain.usecase.GetColorLightnessUseCase

/**
 * DI module for components of __data__ architectural layer.
 */
@Module(
    includes = [
        DiDataRemoteModule::class,
        DiDataLocalModule::class,
        DiDataModule.ProvideModule::class,
        DiDataModule.BindModule::class,
    ],
)
@InstallIn(SingletonComponent::class)
object DiDataModule {

    @Module
    @DisableInstallInCheck
    object ProvideModule

    @Module
    @DisableInstallInCheck
    interface BindModule {

        @Binds
        fun bindGetColorLightnessUseCase(impl: GetColorLightnessUseCaseImpl): GetColorLightnessUseCase
    }
}