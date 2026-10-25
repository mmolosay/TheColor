package io.github.mmolosay.thecolor.presentation.input.group

import io.github.mmolosay.thecolor.domain.color.ColorInputType
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.filterReady
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ColorInputGroupDataFactory @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend fun create(): ColorInputGroupData {
        val preferredInputType = userPreferencesRepository.flowOfColorInputType
            .filterReady().first()
            .getOrElse { DefaultUserPreferences.PreferredColorInputType }
        // make list of all input types with the preferred one being first
        val orderedInputTypes = run {
            val allInputTypes = ColorInputType.entries
            val allInputTypesWithoutPreferredOne = allInputTypes.filter { it != preferredInputType }
            listOf(preferredInputType) + allInputTypesWithoutPreferredOne
        }
        return ColorInputGroupData(
            selectedInputType = preferredInputType,
            orderedInputTypes = orderedInputTypes,
        )
    }
}