package com.ordolabs.domain.usecase.local

import com.ordolabs.domain.model.ColorRgb
import com.ordolabs.domain.repository.ColorValidatorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ValidateColorRgbUseCase @Inject constructor(
    private val colorValidatorRepository: ColorValidatorRepository
) {

    fun invoke(param: ColorRgb?): Flow<Boolean> =
        colorValidatorRepository.validateColor(param)
}