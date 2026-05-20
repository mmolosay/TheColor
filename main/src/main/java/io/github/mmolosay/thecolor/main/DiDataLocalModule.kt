package io.github.mmolosay.thecolor.main

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.migration.DisableInstallInCheck
import io.github.mmolosay.thecolor.data.local.BuildInfoRepositoryImpl
import io.github.mmolosay.thecolor.data.local.DevOptionsDataStoreRepository
import io.github.mmolosay.thecolor.data.local.LastSearchedColorDataStoreRepository
import io.github.mmolosay.thecolor.data.local.TouchLocalDatabaseUseCaseImpl
import io.github.mmolosay.thecolor.data.local.UserPreferencesDataStoreRepository
import io.github.mmolosay.thecolor.domain.TouchLocalDatabaseUseCase
import io.github.mmolosay.thecolor.domain.buildfeatures.BuildInfoRepository
import io.github.mmolosay.thecolor.domain.color.LastSearchedColorRepository
import io.github.mmolosay.thecolor.domain.dev.options.DevOptionsRepository
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.main.di.qualifiers.DataStoreDiQualifiers
import javax.inject.Singleton

/**
 * Part of [DiDataModule].
 * Focuses on data components related to local sources.
 */
@Module(
    includes = [DiDataLocalProvideModule::class, DiDataLocalBindModule::class],
)
@DisableInstallInCheck
object DiDataLocalModule

@Module
@DisableInstallInCheck
object DiDataLocalProvideModule {

    @Provides
    @Singleton
    @DataStoreDiQualifiers.UserPreferences
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("user_preferences") },
        )

    @Provides
    @Singleton
    @DataStoreDiQualifiers.DevOptions
    fun provideDevOptionsDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("dev_options") }
        )

    @Provides
    @Singleton
    @DataStoreDiQualifiers.MiscValues
    fun provideMiscValuesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("misc_values") },
        )
}

@Module
@DisableInstallInCheck
interface DiDataLocalBindModule {

    @Binds
    fun bindUserPreferencesRepository(impl: UserPreferencesDataStoreRepository): UserPreferencesRepository

    @Binds
    fun bindDevOptionsRepository(impl: DevOptionsDataStoreRepository): DevOptionsRepository

    @Binds
    fun bindLastSearchedColorRepository(impl: LastSearchedColorDataStoreRepository): LastSearchedColorRepository

    @Binds
    fun bindTouchLocalDatabaseUseCase(impl: TouchLocalDatabaseUseCaseImpl): TouchLocalDatabaseUseCase

    @Binds
    fun bindBuildInfoRepository(impl: BuildInfoRepositoryImpl): BuildInfoRepository
}