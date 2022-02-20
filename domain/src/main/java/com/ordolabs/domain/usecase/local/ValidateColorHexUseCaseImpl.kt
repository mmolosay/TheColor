package com.ordolabs.domain.usecase.local

import com.ordolabs.domain.model.ColorHex
import com.ordolabs.domain.repository.ColorValidatorRepository
import com.ordolabs.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow

interface ValidateColorHexUseCase : BaseUseCase<ColorHex?, Boolean>

class ValidateColorHexUseCaseImpl(
    private val colorValidatorRepository: ColorValidatorRepository
) : ValidateColorHexUseCase {

    override suspend fun invoke(param: ColorHex?): Flow<Boolean> =
        colorValidatorRepository.validateColor(param)
}