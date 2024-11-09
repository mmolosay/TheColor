package io.github.mmolosay.thecolor.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.model.UserPreferences.asSingletonSet
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
import io.github.mmolosay.thecolor.domain.model.ColorInputType as DomainColorInputType
import io.github.mmolosay.thecolor.domain.model.UserPreferences.ResumeFromLastSearchedColorOnStartup as DomainShouldResumeFromLastSearchedColorOnStartup
import io.github.mmolosay.thecolor.domain.model.UserPreferences.SelectAllTextOnTextFieldFocus as DomainSelectAllTextOnTextFieldFocus
import io.github.mmolosay.thecolor.domain.model.UserPreferences.SmartBackspace as DomainSmartBackspace
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiColorScheme as DomainUiColorScheme
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiColorSchemeSet as DomainUiColorSchemeSet

// TODO: add unit tests
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    val dataStateFlow: StateFlow<DataState> =
        combine(
            userPreferencesRepository.flowOfColorInputType(),
            userPreferencesRepository.flowOfAppUiColorSchemeSet(),
            userPreferencesRepository.flowOfResumeFromLastSearchedColorOnStartup(),
            userPreferencesRepository.flowOfSmartBackspace(),
            userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus(),
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

    private fun updateAppUiColorSchemeSet(value: DomainUiColorSchemeSet) {
        viewModelScope.launch(defaultDispatcher) {
            userPreferencesRepository.setAppUiColorSchemeSet(value)
        }
    }

    private fun updateResumeFromLastSearchedColorOnStartupEnablement(value: Boolean) {
        viewModelScope.launch(defaultDispatcher) {
            val domainModel = DomainShouldResumeFromLastSearchedColorOnStartup(value)
            userPreferencesRepository.setResumeFromLastSearchedColorOnStartup(domainModel)
        }
    }

    private fun updateSmartBackspaceEnablement(value: Boolean) {
        viewModelScope.launch(defaultDispatcher) {
            val domainModel = DomainSmartBackspace(value)
            userPreferencesRepository.setSmartBackspace(domainModel)
        }
    }

    private fun updateSelectAllTextOnTextFieldFocusEnablement(value: Boolean) {
        viewModelScope.launch(defaultDispatcher) {
            val domainModel = DomainSelectAllTextOnTextFieldFocus(value)
            userPreferencesRepository.setSelectAllTextOnTextFieldFocus(domainModel)
        }
    }

    private fun createData(
        preferredColorInputType: DomainColorInputType,
        appUiColorSchemeSet: DomainUiColorSchemeSet,
        shouldResumeFromLastSearchedColorOnStartup: DomainShouldResumeFromLastSearchedColorOnStartup,
        smartBackspace: DomainSmartBackspace,
        selectAllTextOnTextFieldFocus: DomainSelectAllTextOnTextFieldFocus,
    ): SettingsData {
        return SettingsData(
            preferredColorInputType = preferredColorInputType,
            changePreferredColorInputType = ::updatePreferredColorInputType,
            appUiColorSchemeSet = appUiColorSchemeSet,
            supportedAppUiColorSchemeSets = supportedAppUiColorSchemeSets(),
            changeAppUiColorSchemeSet = ::updateAppUiColorSchemeSet,
            isResumeFromLastSearchedColorOnStartupEnabled = shouldResumeFromLastSearchedColorOnStartup.enabled,
            changeResumeFromLastSearchedColorOnStartupEnablement = ::updateResumeFromLastSearchedColorOnStartupEnablement,
            isSmartBackspaceEnabled = smartBackspace.enabled,
            changeSmartBackspaceEnablement = ::updateSmartBackspaceEnablement,
            isSelectAllTextOnTextFieldFocusEnabled = selectAllTextOnTextFieldFocus.enabled,
            changeSelectAllTextOnTextFieldFocusEnablement = ::updateSelectAllTextOnTextFieldFocusEnablement,
        )
    }

    private fun supportedAppUiColorSchemeSets(): List<DomainUiColorSchemeSet> =
        buildList {
            DomainUiColorScheme.Light.asSingletonSet()
                .also { add(it) }
            DomainUiColorScheme.Dark.asSingletonSet()
                .also { add(it) }
            DomainUiColorSchemeSet.DayNight
                .also { add(it) }
        }

    sealed interface DataState {
        data object Loading : DataState
        data class Ready(val data: SettingsData) : DataState
    }
}