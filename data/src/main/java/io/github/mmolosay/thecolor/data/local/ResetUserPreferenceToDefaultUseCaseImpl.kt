package io.github.mmolosay.thecolor.data.local

import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.usecase.ResetUserPreferencesToDefaultUseCase
import javax.inject.Inject

class ResetUserPreferenceToDefaultUseCaseImpl @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ResetUserPreferencesToDefaultUseCase {

    override suspend fun invoke() {
        // TODO: try concurrent updates
        userPreferencesRepository.setColorInputType(null)
        userPreferencesRepository.setAppUiColorSchemeSet(null)
        userPreferencesRepository.setResumeFromLastSearchedColorOnStartup(null)
        userPreferencesRepository.setSmartBackspace(null)
        userPreferencesRepository.setSelectAllTextOnTextFieldFocus(null)
    }
}