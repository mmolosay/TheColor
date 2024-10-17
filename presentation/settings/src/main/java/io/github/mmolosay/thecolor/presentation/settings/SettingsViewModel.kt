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
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiTheme as DomainUiTheme
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiThemeMode as DomainUiThemeMode

// TODO: add unit tests
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    val dataStateFlow: StateFlow<DataState> =
        combine(
            userPreferencesRepository.flowOfColorInputType(),
            userPreferencesRepository.flowOfAppUiThemeMode(),
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

    private fun updateAppUiThemeMode(value: DomainUiThemeMode) {
        viewModelScope.launch(defaultDispatcher) {
            userPreferencesRepository.setAppUiThemeMode(value)
        }
    }

    private fun createData(
        preferredColorInputType: DomainColorInputType,
        appUiThemeMode: DomainUiThemeMode,
    ): SettingsData {
        return SettingsData(
            preferredColorInputType = preferredColorInputType,
            changePreferredColorInputType = ::updatePreferredColorInputType,
            appUiThemeMode = appUiThemeMode,
            supportedAppUiThemeModes = supportedAppUiThemeModes(),
            changeAppUiThemeMode = ::updateAppUiThemeMode,
        )
    }

    private fun supportedAppUiThemeModes(): List<DomainUiThemeMode> =
        buildList {
            DomainUiThemeMode.Single(theme = DomainUiTheme.Light)
                .also { add(it) }
            DomainUiThemeMode.Single(theme = DomainUiTheme.Dark)
                .also { add(it) }
            DomainUiThemeMode.DayNight
                .also { add(it) }
        }

    sealed interface DataState {
        data object Loading : DataState
        data class Ready(val data: SettingsData) : DataState
    }
}