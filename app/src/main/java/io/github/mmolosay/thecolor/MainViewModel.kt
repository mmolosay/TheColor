package io.github.mmolosay.thecolor

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.filterReady
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import io.github.mmolosay.thecolor.presentation.design.ColorSchemeResolver
import io.github.mmolosay.thecolor.presentation.design.toPresentation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.DynamicUiColors as DomainDynamicUiColors

@HiltViewModel
class MainViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val flowOfAppUiColorSchemeResolver: Flow<ColorSchemeResolver> =
        userPreferencesRepository
            .flowOfAppUiColorSchemeSet
            .filterReady()
            .map { it.result.getOrElse { DefaultUserPreferences.AppUiColorSchemeSet } }
            .map { it.toPresentation() }

    val flowOfDynamicUiColors: Flow<DomainDynamicUiColors> =
        userPreferencesRepository
            .flowOfDynamicUiColors
            .filterReady()
            .map { it.result.getOrElse { DefaultUserPreferences.DynamicUiColors } }
}