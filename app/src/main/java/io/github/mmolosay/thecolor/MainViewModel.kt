package io.github.mmolosay.thecolor

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiThemeMode as DomainUiThemeMode

@HiltViewModel
class MainViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val appUiThemeModeFlow: Flow<DomainUiThemeMode> =
        userPreferencesRepository.flowOfAppUiThemeMode()
}