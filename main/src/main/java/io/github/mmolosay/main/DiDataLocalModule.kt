package io.github.mmolosay.main

import android.content.Context
import androidx.room.Room
import com.ordolabs.data_local.TheColorDatabase
import com.ordolabs.data_local.dao.ColorsHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Part of [DiDataModule].
 * Focuses on data components related to local sources.
 */
@Module
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