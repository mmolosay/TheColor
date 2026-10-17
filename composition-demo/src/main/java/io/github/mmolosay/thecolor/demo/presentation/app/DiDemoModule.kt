package io.github.mmolosay.thecolor.demo.presentation.app

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.migration.DisableInstallInCheck
import io.github.mmolosay.thecolor.demo.data.FakeDemoSettings
import io.github.mmolosay.thecolor.demo.data.FakeNutritionService
import io.github.mmolosay.thecolor.demo.data.SharedPreferencesRecipeRepository
import io.github.mmolosay.thecolor.demo.domain.DemoSettings
import io.github.mmolosay.thecolor.demo.domain.NutritionService
import io.github.mmolosay.thecolor.demo.domain.RecipeRepository
import io.github.mmolosay.thecolor.main.di.qualifiers.AppCoroutineScope
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module(
    includes = [
        DiDemoModule.ProvideModule::class,
        DiDemoModule.BindModule::class,
    ],
)
@InstallIn(SingletonComponent::class)
object DiDemoModule {

    @Module
    @DisableInstallInCheck
    object ProvideModule {

        @Provides
        @Singleton
        @AppCoroutineScope
        fun provideAppCoroutineScope(): CoroutineScope =
            CoroutineScope(SupervisorJob() + Dispatchers.Default)

        @Provides
        @IoDispatcher
        fun provideIoDispatcher(): CoroutineDispatcher =
            Dispatchers.IO
    }

    @Module
    @DisableInstallInCheck
    interface BindModule {

        @Binds
        fun bindNutritionService(impl: FakeNutritionService): NutritionService

        @Binds
        fun bindDemoSettings(impl: FakeDemoSettings): DemoSettings

        @Binds
        fun bindRecipeRepository(impl: SharedPreferencesRecipeRepository): RecipeRepository
    }
}
