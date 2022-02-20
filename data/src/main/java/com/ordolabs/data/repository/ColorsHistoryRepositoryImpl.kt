package com.ordolabs.data.repository

import com.ordolabs.data_local.dao.ColorsHistoryDao
import com.ordolabs.data_local.mapper.toDomain
import com.ordolabs.domain.model.HistoryColor
import com.ordolabs.domain.repository.ColorsHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ColorsHistoryRepositoryImpl(
    private val colorsHistoryDao: ColorsHistoryDao
) : ColorsHistoryRepository {

    override fun getColorsFromHistory(): Flow<List<HistoryColor>> = flow {
        val colors = colorsHistoryDao.getAll()
        emit(colors.map { it.toDomain() })
    }
}