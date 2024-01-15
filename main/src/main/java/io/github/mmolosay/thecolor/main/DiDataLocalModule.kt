package io.github.mmolosay.thecolor.main

import android.content.Context
import androidx.room.Room
import io.github.mmolosay.thecolor.data.local.TheColorDatabase
import io.github.mmolosay.thecolor.data.local.dao.ColorsHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.migration.DisableInstallInCheck

/**
 * Part of [DiDataModule].
 * Focuses on data components related to local sources.
 */
@Module
@DisableInstallInCheck
object DiDataLocalModule {

    // region Database

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

    private const val DATABASE_NAME = "the_color_db"

    // endregion

    // region Preferences


    // endregion
}