package io.github.mmolosay.thecolor

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.presentation.design.ColorSchemeResolver
import io.github.mmolosay.thecolor.presentation.design.toPresentation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import io.github.mmolosay.thecolor.domain.model.UserPreferences.DynamicUiColors as DomainDynamicUiColors

@HiltViewModel
class MainViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val flowOfAppUiColorSchemeResolver: Flow<ColorSchemeResolver> =
        userPreferencesRepository
            .flowOfAppUiColorSchemeSet()
            .map { it.toPresentation() }

    val flowOfDynamicUiColors: Flow<DomainDynamicUiColors> =
        userPreferencesRepository
            .flowOfDynamicUiColors()
}