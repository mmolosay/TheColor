package io.github.mmolosay.thecolor.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.github.mmolosay.thecolor.data.local.model.ColorHistoryEntity

@Dao
interface ColorsHistoryDao {

    @Transaction
    @Query("SELECT * FROM colors_history")
    suspend fun getAll(): List<ColorHistoryEntity>
}