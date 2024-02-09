package io.github.mmolosay.thecolor.domain.repository

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.domain.model.ColorScheme
import io.github.mmolosay.thecolor.domain.model.OldColorScheme
import io.github.mmolosay.thecolor.domain.model.ColorSchemeRequest
import io.github.mmolosay.thecolor.domain.usecase.GetColorSchemeUseCase

interface ColorRepository {
    suspend fun lastSearchedColor(): Color.Abstract?
    suspend fun getColorDetails(colorHex: String): ColorDetails
    suspend fun getColorDetails(color: Color): ColorDetails
    suspend fun getColorScheme(request: ColorSchemeRequest): OldColorScheme
    suspend fun getColorScheme(request: GetColorSchemeUseCase.Request): ColorScheme
}