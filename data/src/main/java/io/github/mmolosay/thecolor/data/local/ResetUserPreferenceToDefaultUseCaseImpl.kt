package io.github.mmolosay.thecolor.data.local

import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.usecase.ResetUserPreferencesToDefaultUseCase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.TimeSource
import kotlin.time.measureTime

class ResetUserPreferenceToDefaultUseCaseImpl @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ResetUserPreferencesToDefaultUseCase {

    override suspend fun invoke() {
        // updating values sequentially and concurrently take about the same time ~68ms
        userPreferencesRepository.setColorInputType(null)
        userPreferencesRepository.setAppUiColorSchemeSet(null)
        userPreferencesRepository.setResumeFromLastSearchedColorOnStartup(null)
        userPreferencesRepository.setSmartBackspace(null)
        userPreferencesRepository.setSelectAllTextOnTextFieldFocus(null)
    }
}