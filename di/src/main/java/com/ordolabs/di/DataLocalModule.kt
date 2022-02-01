package com.ordolabs.di

import android.content.Context
import androidx.room.Room
import com.ordolabs.data_local.TheColorDatabase
import com.ordolabs.data_local.dao.ColorsHistoryDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataLocalModule {

    // region Database

    @Provides
    @Singleton
    fun provideDatabase(
        context: Context
    ): TheColorDatabase =
        Room
            .databaseBuilder(
                context.applicationContext,
                TheColorDatabase::class.java,
                DATABASE_NAME
            )
            .build()

    // region DAOs

    @Provides
    @Singleton
    fun provideColorsHistoryDao(
        db: TheColorDatabase
    ): ColorsHistoryDao =
        db.colorsHistoryDao()

    companion object {
        private const val DATABASE_NAME = "the_color_db"
    }

    // endregion

    // endregion

    // region DataStore



    // endregion
}