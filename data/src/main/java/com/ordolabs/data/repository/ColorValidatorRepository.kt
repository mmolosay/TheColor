package com.ordolabs.data.repository

import com.ordolabs.domain.model.ColorHex
import com.ordolabs.domain.model.ColorRgb
import com.ordolabs.domain.repository.IColorValidatorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ColorValidatorRepository : IColorValidatorRepository {

    override fun validateColor(color: ColorHex): Flow<Boolean> = flow {
        val value = color.value
        val valid = Regex(HEX_COLOR_VALIDATION_REGEX_PATTERN).matches(value)
        emit(valid)
    }

    override fun validateColor(color: ColorRgb): Flow<Boolean> = flow {
        val componentRange = 0..256
        emit(color.r in componentRange && color.g in componentRange && color.b in componentRange)
    }

    companion object {

        private const val HEX_COLOR_VALIDATION_REGEX_PATTERN = "^([a-fA-F0-9]{6}|[a-fA-F0-9]{3})\$"
    }
}