package io.github.mmolosay.thecolor.main

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.migration.DisableInstallInCheck
import io.github.mmolosay.thecolor.data.local.TheColorDatabase
import io.github.mmolosay.thecolor.data.local.UserPreferencesDataStoreRepository
import io.github.mmolosay.thecolor.data.local.dao.ColorsHistoryDao
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import java.lang.ref.WeakReference
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
    @Singleton // to avoid multiple instances of DataStores existing at the same time
    fun provideUserPreferencesDataStoreRepository(
        @ApplicationContext context: Context,
        @Named("ioDispatcher") ioDispatcher: CoroutineDispatcher,
    ) =
        UserPreferencesDataStoreRepository(
            appContext = WeakReference(context),
            ioDispatcher = ioDispatcher,
        )

    private const val DATABASE_NAME = "the_color_db"
}

@Module
@DisableInstallInCheck
interface DiDataLocalBindModule {

    @Binds
    fun bindUserPreferencesRepository(impl: UserPreferencesDataStoreRepository): UserPreferencesRepository
}