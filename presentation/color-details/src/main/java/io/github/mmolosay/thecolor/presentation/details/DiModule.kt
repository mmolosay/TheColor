package io.github.mmolosay.thecolor.presentation.details

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.migration.DisableInstallInCheck
import io.github.mmolosay.thecolor.presentation.common.viewmodel.CommandStore
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCommandStore

@Module(
    includes = [DiModule.ProvideModule::class, DiModule.BindModule::class],
)
@InstallIn(SingletonComponent::class)
object DiModule {

    @Module
    @DisableInstallInCheck
    object ProvideModule {

        @Provides
        fun provideCommandStore(): ColorDetailsCommandStore =
            CommandStore()
    }

    @Module
    @DisableInstallInCheck
    interface BindModule
}