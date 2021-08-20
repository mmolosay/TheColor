package com.ordolabs.domain.usecase

import com.ordolabs.domain.model.ColorHex
import com.ordolabs.domain.repository.IColorInfoRepository

typealias GetColorInfoBaseUseCase = BaseUseCase<String, ColorHex>

class GetColorInfoUseCase(
    private val colorInfoRepository: IColorInfoRepository
) : GetColorInfoBaseUseCase {

    override suspend fun invoke(param: String) =
        colorInfoRepository.fetchColorInfo(param)
}