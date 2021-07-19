package com.ordolabs.domain.usecase

import com.ordolabs.domain.model.Color
import com.ordolabs.domain.repository.IColorInfoRepository
import kotlinx.coroutines.flow.Flow

typealias GetColorInfoBaseUseCase = BaseUseCase<String, Flow<Color>>

class GetColorInfoUseCase(
    private val colorInfoRepository: IColorInfoRepository
) : GetColorInfoBaseUseCase {

    override suspend fun invoke(param: String) =
        colorInfoRepository.fetchColorInfo(param)
}