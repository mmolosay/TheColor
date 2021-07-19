package com.ordolabs.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "colors_history")
data class ColorHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)