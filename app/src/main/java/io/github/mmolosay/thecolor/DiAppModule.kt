package io.github.mmolosay.thecolor

import android.app.Application
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.migration.DisableInstallInCheck
import kotlinx.coroutines.CoroutineScope
import javax.inject.Named

/**
 * DI module for components of `:app` Gradle module.
 */
@Module(
    includes = [
        DiAppModule.ProvideModule::class,
        DiAppModule.BindModule::class,
    ],
)
@InstallIn(SingletonComponent::class)
object DiAppModule {

    @Module
    @DisableInstallInCheck
    object ProvideModule {

        @Provides
        fun provideTheColorApplication(application: Application): TheColorApplication =
            application as TheColorApplication

        @Provides
        @Named("ApplicationScope")
        fun provideApplicationScope(provider: ApplicationCoroutineScopeProvider): CoroutineScope =
            provider.applicationScope
    }

    @Module
    @DisableInstallInCheck
    interface BindModule {

        @Binds
        fun bindApplicationCoroutineScopeProvider(application: TheColorApplication): ApplicationCoroutineScopeProvider
    }
}