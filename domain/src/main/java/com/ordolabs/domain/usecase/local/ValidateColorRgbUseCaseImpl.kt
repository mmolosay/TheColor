package com.ordolabs.domain.usecase.local

import com.ordolabs.domain.model.ColorRgb
import com.ordolabs.domain.repository.IColorValidatorRepository
import com.ordolabs.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow

interface ValidateColorRgbUseCase : BaseUseCase<ColorRgb?, Boolean>

class ValidateColorRgbUseCaseImpl(
    private val colorValidatorRepository: IColorValidatorRepository
) : ValidateColorRgbUseCase {

    override suspend fun invoke(param: ColorRgb?): Flow<Boolean> =
        colorValidatorRepository.validateColor(param)
}