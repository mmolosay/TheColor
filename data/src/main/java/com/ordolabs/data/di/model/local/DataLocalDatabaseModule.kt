package com.ordolabs.data.di.model.local

import android.content.Context
import androidx.room.Room
import com.ordolabs.data_local.TheColorDatabase
import com.ordolabs.data_local.dao.ColorsHistoryDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataLocalDatabaseModule {

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

    // endregion

    companion object {
        private const val DATABASE_NAME = "the_color_db"
    }
}