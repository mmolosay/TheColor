package com.ordolabs.di

import androidx.room.Room
import com.ordolabs.data_local.TheColorDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val localDataSourceModule = module {

    single {
        Room
            .databaseBuilder(androidContext(), TheColorDatabase::class.java, DATABASE_NAME)
            .build()
    }

    single { provideColorsHistoryDao(db = get()) }

}

private const val DATABASE_NAME = "the_color_db"

internal fun provideColorsHistoryDao(db: TheColorDatabase) =
    db.colorsHistoryDao()