package com.ordolabs.data.repository

import com.ordolabs.data_local.dao.ColorsHistoryDao
import com.ordolabs.data_local.mapper.toDomain
import com.ordolabs.domain.model.HistoryColor
import com.ordolabs.domain.repository.IColorsHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ColorsHistoryRepository(
    private val colorsHistoryDao: ColorsHistoryDao
) : IColorsHistoryRepository {

    override fun getColorsFromHistory(): Flow<List<HistoryColor>> = flow {
        val colors = colorsHistoryDao.getAll()
        emit(colors.map { it.toDomain() })
    }
}