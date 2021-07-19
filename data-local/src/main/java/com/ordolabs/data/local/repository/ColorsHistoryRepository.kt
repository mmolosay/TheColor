package com.ordolabs.data.local.repository

import com.ordolabs.data.local.dao.ColorsHistoryDao
import com.ordolabs.data.local.mapper.toDomain
import com.ordolabs.domain.model.ColorHistory
import com.ordolabs.domain.repository.IColorsHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ColorsHistoryRepository(
    private val colorsHistoryDao: ColorsHistoryDao
) : IColorsHistoryRepository {

    override fun getColorsFromHistory(): Flow<List<ColorHistory>> = flow {
        val colors = colorsHistoryDao.getAll()
        emit(colors.map { it.toDomain() })
    }
}