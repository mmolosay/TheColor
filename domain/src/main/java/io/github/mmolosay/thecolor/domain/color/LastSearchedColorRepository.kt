package io.github.mmolosay.thecolor.domain.color

interface LastSearchedColorRepository {
    suspend fun getLastSearchedColor(): Color?
    suspend fun setLastSearchedColor(color: Color?)
}