package io.github.mmolosay.thecolor.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.presentation.settings.SettingsData.LabeledAppUiThemeMode
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

    private fun updateAppUiThemeMode(value: LabeledAppUiThemeMode) {
        viewModelScope.launch(defaultDispatcher) {
            userPreferencesRepository.setAppUiThemeMode(value.mode)
        }
    }

    private fun createData(
        preferredColorInputType: DomainColorInputType,
        appUiThemeMode: DomainUiThemeMode,
    ): SettingsData {
        val labeledMode = LabeledAppUiThemeMode(
            mode = appUiThemeMode,
            label = appUiThemeMode.label(),
        )
        return SettingsData(
            preferredColorInputType = preferredColorInputType,
            changePreferredColorInputType = ::updatePreferredColorInputType,
            appUiThemeMode = labeledMode,
            supportedAppUiThemeModes = supportedAppUiThemeModes(),
            changeAppUiThemeMode = ::updateAppUiThemeMode,
        )
    }

    private fun supportedAppUiThemeModes(): List<LabeledAppUiThemeMode> =
        buildList {
            // each theme as single
            LabeledAppUiThemeMode(
                mode = DomainUiThemeMode.Single(theme = DomainUiTheme.Light),
                label = LabeledAppUiThemeMode.Label.SingleLight,
            ).also { add(it) }
            LabeledAppUiThemeMode(
                mode = DomainUiThemeMode.Single(theme = DomainUiTheme.Dark),
                label = LabeledAppUiThemeMode.Label.SingleDark,
            ).also { add(it) }
            // Dual with Light and Dark themes
            LabeledAppUiThemeMode(
                mode = DomainUiThemeMode.Dual(
                    light = DomainUiTheme.Light,
                    dark = DomainUiTheme.Dark,
                ),
                label = LabeledAppUiThemeMode.Label.DualLightDark,
            ).also { add(it) }
        }

    private fun DomainUiThemeMode.label(): LabeledAppUiThemeMode.Label =
        when (this) {
            is DomainUiThemeMode.Single -> {
                when (this.theme) {
                    DomainUiTheme.Light -> LabeledAppUiThemeMode.Label.SingleLight
                    DomainUiTheme.Dark -> LabeledAppUiThemeMode.Label.SingleDark
                }
            }
            is DomainUiThemeMode.Dual -> {
                when {
                    this == DomainUiThemeMode.DayNight -> LabeledAppUiThemeMode.Label.DualLightDark
                    else -> error("Unsupported app UI theme mode")
                }
            }
        }

    sealed interface DataState {
        data object Loading : DataState
        data class Ready(val data: SettingsData) : DataState
    }
}