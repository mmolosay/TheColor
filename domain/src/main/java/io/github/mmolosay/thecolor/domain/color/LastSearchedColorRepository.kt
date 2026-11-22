package io.github.mmolosay.thecolor.domain.color

import io.github.mmolosay.thecolor.domain.utils.PrefState
import kotlinx.coroutines.flow.StateFlow

interface LastSearchedColorRepository {
    val flowOfLastSearchedColor: StateFlow<PrefState<Color>>
    suspend fun setLastSearchedColor(color: Color?)
}
