package io.github.mmolosay.thecolor.domain.color

import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.filterReady
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetStartupColorUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val lastSearchedColorRepository: LastSearchedColorRepository,
) {

    suspend operator fun invoke(): Color? {
        val preference = userPreferencesRepository
            .flowOfResumeFromLastSearchedColorOnStartup
            .filterReady().first()
            .getOrElse { DefaultUserPreferences.ResumeFromLastSearchedColorOnStartup }
        if (!preference.enabled) return null
        return lastSearchedColorRepository.getLastSearchedColor()
    }
}