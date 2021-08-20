package com.ordolabs.domain.usecase.local

import com.ordolabs.domain.model.ColorHex
import com.ordolabs.domain.model.ColorRgb
import com.ordolabs.domain.repository.IColorValidatorRepository
import com.ordolabs.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow

typealias ValidateColorRgbBaseUseCase = BaseUseCase<ColorRgb, Boolean>

class ValidateColorRgbUseCase(
    private val colorValidatorRepository: IColorValidatorRepository
) : ValidateColorRgbBaseUseCase {

    override suspend fun invoke(param: ColorRgb): Flow<Boolean> =
        colorValidatorRepository.validateColor(param)
}