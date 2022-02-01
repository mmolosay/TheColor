package com.ordolabs.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.ordolabs.data_local.TheColorDatabase
import dagger.Module
import dagger.Provides
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

@Module
object LocalDataSourceModule {

    // TODO: must provide the same instance each time; singleton
    @Provides
    fun provideDatabase(): RoomDatabase {

    }
}

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