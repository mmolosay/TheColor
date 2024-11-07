package io.github.mmolosay.thecolor.domain.repository

import io.github.mmolosay.thecolor.domain.model.Color

interface LastSearchedColorRepository {
    suspend fun getLastSearchedColor(): Color?
    suspend fun setLastSearchedColor(color: Color?)
}