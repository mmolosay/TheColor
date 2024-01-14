package com.ordolabs.domain.usecase.local

import com.ordolabs.domain.model.ColorHex
import com.ordolabs.domain.repository.ColorValidatorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ValidateColorHexUseCase @Inject constructor(
    private val colorValidatorRepository: ColorValidatorRepository
) {

    fun invoke(param: ColorHex?): Flow<Boolean> =
        colorValidatorRepository.validateColor(param)
}