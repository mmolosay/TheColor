package io.github.mmolosay.thecolor.data

import io.github.mmolosay.thecolor.data.local.dao.ColorsHistoryDao
import io.github.mmolosay.thecolor.data.local.mapper.toDomain
import io.github.mmolosay.thecolor.domain.model.HistoryColor
import io.github.mmolosay.thecolor.domain.repository.ColorsHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ColorsHistoryRepositoryImpl @Inject constructor(
    private val colorsHistoryDao: ColorsHistoryDao
) : ColorsHistoryRepository {

    override fun getColorsFromHistory(): Flow<List<HistoryColor>> = flow {
        val colors = colorsHistoryDao.getAll()
        emit(colors.map { it.toDomain() })
    }
}