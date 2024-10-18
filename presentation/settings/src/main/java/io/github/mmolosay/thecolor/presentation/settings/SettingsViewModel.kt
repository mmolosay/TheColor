package io.github.mmolosay.thecolor.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import io.github.mmolosay.thecolor.domain.model.UserPreferences.ColorInputType as DomainColorInputType
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiColorScheme as DomainUiColorScheme
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiColorSchemeMode as DomainUiColorSchemeMode

// TODO: add unit tests
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    val dataStateFlow: StateFlow<DataState> =
        combine(
            userPreferencesRepository.flowOfColorInputType(),
            userPreferencesRepository.flowOfAppUiColorSchemeMode(),
            transform = ::createData,
        )
            .map { data -> DataState.Ready(data) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DataState.Loading,
            )

    private fun updatePreferredColorInputType(value: DomainColorInputType) {
        viewModelScope.launch(defaultDispatcher) {
            userPreferencesRepository.setColorInputType(value)
        }
    }

    private fun updateAppUiThemeMode(value: DomainUiColorSchemeMode) {
        viewModelScope.launch(defaultDispatcher) {
            userPreferencesRepository.setAppUiColorSchemeMode(value)
        }
    }

    private fun createData(
        preferredColorInputType: DomainColorInputType,
        appUiThemeMode: DomainUiColorSchemeMode,
    ): SettingsData {
        return SettingsData(
            preferredColorInputType = preferredColorInputType,
            changePreferredColorInputType = ::updatePreferredColorInputType,
            appUiThemeMode = appUiThemeMode,
            supportedAppUiThemeModes = supportedAppUiThemeModes(),
            changeAppUiThemeMode = ::updateAppUiThemeMode,
        )
    }

    private fun supportedAppUiThemeModes(): List<DomainUiColorSchemeMode> =
        buildList {
            DomainUiColorSchemeMode.Single(theme = DomainUiColorScheme.Light)
                .also { add(it) }
            DomainUiColorSchemeMode.Single(theme = DomainUiColorScheme.Dark)
                .also { add(it) }
            DomainUiColorSchemeMode.DayNight
                .also { add(it) }
        }

    sealed interface DataState {
        data object Loading : DataState
        data class Ready(val data: SettingsData) : DataState
    }
}