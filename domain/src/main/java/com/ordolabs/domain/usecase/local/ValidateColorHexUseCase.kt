package com.ordolabs.domain.usecase.local

import com.ordolabs.domain.model.ColorHex
import com.ordolabs.domain.repository.IColorValidatorRepository
import com.ordolabs.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow

typealias ValidateColorHexBaseUseCase = BaseUseCase<ColorHex?, Boolean>

class ValidateColorHexUseCase(
    private val colorValidatorRepository: IColorValidatorRepository
) : ValidateColorHexBaseUseCase {

    override suspend fun invoke(param: ColorHex?): Flow<Boolean> =
        colorValidatorRepository.validateColor(param)
}