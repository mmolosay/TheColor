package io.github.mmolosay.thecolor.main

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.migration.DisableInstallInCheck
import io.github.mmolosay.thecolor.data.local.LastSearchedColorDataStoreRepository
import io.github.mmolosay.thecolor.data.local.TheColorDatabase
import io.github.mmolosay.thecolor.data.local.TouchLocalDatabaseUseCaseImpl
import io.github.mmolosay.thecolor.data.local.UserPreferencesDataStoreRepository
import io.github.mmolosay.thecolor.data.local.dao.ColorsHistoryDao
import io.github.mmolosay.thecolor.domain.repository.LastSearchedColorRepository
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.usecase.TouchLocalDatabaseUseCase
import javax.inject.Named
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
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): TheColorDatabase =
        Room
            .databaseBuilder(
                context,
                TheColorDatabase::class.java,
                DATABASE_NAME,
            )
            .build()

    @Provides
    fun provideColorsHistoryDao(
        db: TheColorDatabase,
    ): ColorsHistoryDao =
        db.colorsHistoryDao()

    @Provides
    @Named("UserPreferences")
    @Singleton
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("user_preferences") },
        )

    @Provides
    @Named("MiscValues")
    @Singleton
    fun provideMiscValuesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("misc_values") },
        )

    private const val DATABASE_NAME = "the_color_db"
}

@Module
@DisableInstallInCheck
interface DiDataLocalBindModule {

    @Binds
    fun bindUserPreferencesRepository(impl: UserPreferencesDataStoreRepository): UserPreferencesRepository

    @Binds
    fun bindLastSearchedColorRepository(impl: LastSearchedColorDataStoreRepository): LastSearchedColorRepository

    @Binds
    fun bindTouchLocalDatabaseUseCase(impl: TouchLocalDatabaseUseCaseImpl): TouchLocalDatabaseUseCase
}