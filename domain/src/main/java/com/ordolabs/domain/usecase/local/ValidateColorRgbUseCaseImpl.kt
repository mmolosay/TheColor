package com.ordolabs.domain.usecase.local

import com.ordolabs.domain.model.ColorRgb
import com.ordolabs.domain.repository.ColorValidatorRepository
import com.ordolabs.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ValidateColorRgbUseCase : BaseUseCase<ColorRgb?, Boolean>

class ValidateColorRgbUseCaseImpl @Inject constructor(
    private val colorValidatorRepository: ColorValidatorRepository
) : ValidateColorRgbUseCase {

    override suspend fun invoke(param: ColorRgb?): Flow<Boolean> =
        colorValidatorRepository.validateColor(param)
}