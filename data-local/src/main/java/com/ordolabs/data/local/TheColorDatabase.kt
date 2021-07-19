package com.ordolabs.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ordolabs.data.local.dao.ColorsHistoryDao
import com.ordolabs.data.local.model.ColorHistoryEntity

@Database(entities = [ColorHistoryEntity::class], version = 1, exportSchema = false)
abstract class TheColorDatabase : RoomDatabase() {
    abstract fun colorsHistoryDao(): ColorsHistoryDao
}