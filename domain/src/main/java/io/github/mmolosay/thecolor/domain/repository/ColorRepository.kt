package io.github.mmolosay.thecolor.domain.repository

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.domain.model.ColorScheme
import io.github.mmolosay.thecolor.domain.result.Result
import io.github.mmolosay.thecolor.domain.usecase.GetColorSchemeUseCase

interface ColorRepository {
    suspend fun lastSearchedColor(): Color?
    suspend fun getColorDetails(color: Color): Result<ColorDetails>
    suspend fun getColorScheme(request: GetColorSchemeUseCase.Request): Result<ColorScheme>
}