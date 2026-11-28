package io.github.mmolosay.thecolor

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.readyValues
import io.github.mmolosay.thecolor.presentation.design.ColorSchemeResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val flowOfColorSchemeResolver: Flow<ColorSchemeResolver> =
        combine(
            userPreferencesRepository.flowOfAppUiColorSchemeSet
                .readyValues(DefaultUserPreferences.AppUiColorSchemeSet),
            userPreferencesRepository.flowOfDynamicUiColors
                .readyValues(DefaultUserPreferences.DynamicUiColors),
        ) { colorSchemeSet, dynamicUiColors ->
            ColorSchemeResolver(
                colorSchemeSet = colorSchemeSet,
                useDynamicColorSchemes = dynamicUiColors.enabled,
            )
        }
}