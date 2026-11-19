package io.github.mmolosay.thecolor.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.readyValues
import io.github.mmolosay.thecolor.domain.utils.valueOrNull
import io.github.mmolosay.thecolor.utils.mapState
import io.github.mmolosay.thecolor.utils.requireSend
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.AutoProceedWithRandomizedColors as DomainAutoProceedWithRandomizedColors
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.DynamicUiColors as DomainDynamicUiColors
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.ResumeFromLastSearchedColorOnStartup as DomainResumeFromLastSearchedColorOnStartup
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SelectAllTextOnTextFieldFocus as DomainSelectAllTextOnTextFieldFocus
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SmartBackspace as DomainSmartBackspace
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.UiColorSchemeSet as DomainUiColorSchemeSet

@HiltViewModel
class SettingsViewModel @Inject constructor(
    coreFactory: SettingsCore.Factory,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val core = coreFactory.create()
    private val store = run {
        val state = SettingsState(
            preferredColorInputType = userPreferencesRepository.flowOfColorInputType
                .value.valueOrNull(DefaultUserPreferences.PreferredColorInputType),
            appUiColorSchemeSet = userPreferencesRepository.flowOfAppUiColorSchemeSet
                .value.valueOrNull(DefaultUserPreferences.AppUiColorSchemeSet),
            dynamicUiColors = userPreferencesRepository.flowOfDynamicUiColors
                .value.valueOrNull(DefaultUserPreferences.DynamicUiColors),
            resumeFromLastSearchedColor = userPreferencesRepository.flowOfResumeFromLastSearchedColorOnStartup
                .value.valueOrNull(DefaultUserPreferences.ResumeFromLastSearchedColorOnStartup),
            smartBackspace = userPreferencesRepository.flowOfSmartBackspace
                .value.valueOrNull(DefaultUserPreferences.SmartBackspace),
            selectAllTextOnTextFieldFocus = userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus
                .value.valueOrNull(DefaultUserPreferences.SelectAllTextOnTextFieldFocus),
            autoProceedWithRandomizedColors = userPreferencesRepository.flowOfAutoProceedWithRandomizedColors
                .value.valueOrNull(DefaultUserPreferences.AutoProceedWithRandomizedColors),
        )
        Store(state)
    }
    private val writes = Channel<suspend () -> Unit>(capacity = Channel.UNLIMITED)

    val viewData: StateFlow<SettingsViewData?> =
        store.mapState { state -> with(core) { state.toViewData() } }

    init {
        collectPreferences()
        viewModelScope.launch {
            for (write in writes) write()
        }
    }

    private fun collectPreferences() {
        viewModelScope.launch {
            launch {
                userPreferencesRepository.flowOfColorInputType
                    .readyValues(DefaultUserPreferences.PreferredColorInputType)
                    .collect { value ->
                        store.update { state ->
                            with(core) { state.applyPreferredColorInputType(value) }
                        }
                    }
            }
            launch {
                userPreferencesRepository.flowOfAppUiColorSchemeSet
                    .readyValues(DefaultUserPreferences.AppUiColorSchemeSet)
                    .collect { value ->
                        store.update { state ->
                            with(core) { state.applyAppUiColorSchemeSet(value) }
                        }
                    }
            }
            launch {
                userPreferencesRepository.flowOfDynamicUiColors
                    .readyValues(DefaultUserPreferences.DynamicUiColors)
                    .collect { value ->
                        store.update { state ->
                            with(core) { state.applyDynamicUiColors(value) }
                        }
                    }
            }
            launch {
                userPreferencesRepository.flowOfResumeFromLastSearchedColorOnStartup
                    .readyValues(DefaultUserPreferences.ResumeFromLastSearchedColorOnStartup)
                    .collect { value ->
                        store.update { state ->
                            with(core) { state.applyResumeFromLastSearchedColor(value) }
                        }
                    }
            }
            launch {
                userPreferencesRepository.flowOfSmartBackspace
                    .readyValues(DefaultUserPreferences.SmartBackspace)
                    .collect { value ->
                        store.update { state ->
                            with(core) { state.applySmartBackspace(value) }
                        }
                    }
            }
            launch {
                userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus
                    .readyValues(DefaultUserPreferences.SelectAllTextOnTextFieldFocus)
                    .collect { value ->
                        store.update { state ->
                            with(core) { state.applySelectAllTextOnTextFieldFocus(value) }
                        }
                    }
            }
            launch {
                userPreferencesRepository.flowOfAutoProceedWithRandomizedColors
                    .readyValues(DefaultUserPreferences.AutoProceedWithRandomizedColors)
                    .collect { value ->
                        store.update { state ->
                            with(core) { state.applyAutoProceedWithRandomizedColors(value) }
                        }
                    }
            }
        }
    }

    fun changePreferredColorInputType(value: DomainColorInputType) {
        writes.requireSend {
            userPreferencesRepository.setColorInputType(value)
        }
    }

    fun changeAppUiColorSchemeSet(value: DomainUiColorSchemeSet) {
        writes.requireSend {
            userPreferencesRepository.setAppUiColorSchemeSet(value)
        }
    }

    fun changeDynamicUiColors(enabled: Boolean) {
        writes.requireSend {
            userPreferencesRepository.setDynamicUiColors(DomainDynamicUiColors(enabled))
        }
    }

    fun changeResumeFromLastSearchedColor(enabled: Boolean) {
        writes.requireSend {
            val value = DomainResumeFromLastSearchedColorOnStartup(enabled)
            userPreferencesRepository.setResumeFromLastSearchedColorOnStartup(value)
        }
    }

    fun changeSmartBackspace(enabled: Boolean) {
        writes.requireSend {
            userPreferencesRepository.setSmartBackspace(DomainSmartBackspace(enabled))
        }
    }

    fun changeSelectAllTextOnTextFieldFocus(enabled: Boolean) {
        writes.requireSend {
            val value = DomainSelectAllTextOnTextFieldFocus(enabled)
            userPreferencesRepository.setSelectAllTextOnTextFieldFocus(value)
        }
    }

    fun changeAutoProceedWithRandomizedColors(enabled: Boolean) {
        writes.requireSend {
            val value = DomainAutoProceedWithRandomizedColors(enabled)
            userPreferencesRepository.setAutoProceedWithRandomizedColors(value)
        }
    }

    fun resetToDefaults() {
        writes.requireSend {
            userPreferencesRepository.clear()
        }
    }
}