package io.github.mmolosay.thecolor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.mmolosay.thecolor.data.local.dao.ColorsHistoryDao
import io.github.mmolosay.thecolor.data.local.model.ColorHistoryEntity

@Database(entities = [ColorHistoryEntity::class], version = 1, exportSchema = false)
abstract class TheColorDatabase : RoomDatabase() {
    abstract fun colorsHistoryDao(): ColorsHistoryDao
}