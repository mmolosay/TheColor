package com.ordolabs.thecolor.di

import androidx.room.Room
import com.ordolabs.data.local.TheColorDatabase
import com.ordolabs.data.local.repository.ColorsHistoryRepository
import com.ordolabs.domain.repository.IColorsHistoryRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val localDataSourceModule = module {

    single<IColorsHistoryRepository> {
        ColorsHistoryRepository(colorsHistoryDao = get() )
    }

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