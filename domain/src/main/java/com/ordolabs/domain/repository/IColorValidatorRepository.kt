package com.ordolabs.domain.repository

import com.ordolabs.domain.model.ColorHex
import com.ordolabs.domain.model.ColorRgb
import kotlinx.coroutines.flow.Flow

interface IColorValidatorRepository {

    fun validateColor(color: ColorHex): Flow<Boolean>
    fun validateColor(color: ColorRgb): Flow<Boolean>
}