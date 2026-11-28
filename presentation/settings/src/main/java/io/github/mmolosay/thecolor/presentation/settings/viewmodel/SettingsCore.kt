package io.github.mmolosay.thecolor.presentation.settings.viewmodel

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.buildfeatures.IsDevOptionsEnabledUseCase
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.asSingletonSet
import io.github.mmolosay.thecolor.utils.composition.Change
import kotlinx.collections.immutable.toPersistentList
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.AutoProceedWithRandomizedColors as DomainAutoProceedWithRandomizedColors
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.DynamicUiColors as DomainDynamicUiColors
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.ResumeFromLastSearchedColorOnStartup as DomainResumeFromLastSearchedColorOnStartup
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SelectAllTextOnTextFieldFocus as DomainSelectAllTextOnTextFieldFocus
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SmartBackspace as DomainSmartBackspace
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.UiColorScheme as DomainUiColorScheme
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.UiColorSchemeSet as DomainUiColorSchemeSet

class SettingsCore @AssistedInject constructor(
    private val isDevOptionsEnabled: IsDevOptionsEnabledUseCase,
) {

    // region Getters

    fun SettingsState.toViewData(): SettingsViewData? {
        return SettingsViewData(
            preferredColorInputType = this.preferredColorInputType ?: return null,
            appUiColorSchemeSet = this.appUiColorSchemeSet ?: return null,
            supportedAppUiColorSchemeSets = buildList {
                DomainUiColorSchemeSet.DayNight
                    .also { add(it) }
                DomainUiColorScheme.Light.asSingletonSet()
                    .also { add(it) }
                DomainUiColorScheme.Dark.asSingletonSet()
                    .also { add(it) }
                DomainUiColorScheme.Jungle.asSingletonSet()
                    .also { add(it) }
                DomainUiColorScheme.Midnight.asSingletonSet()
                    .also { add(it) }
            }.toPersistentList(),
            isDynamicUiColorsEnabled = this.dynamicUiColors?.enabled ?: return null,
            isResumeFromLastSearchedColorEnabled = this.resumeFromLastSearchedColor?.enabled
                ?: return null,
            isSmartBackspaceEnabled = this.smartBackspace?.enabled ?: return null,
            isSelectAllTextOnTextFieldFocusEnabled = this.selectAllTextOnTextFieldFocus?.enabled
                ?: return null,
            isAutoProceedWithRandomizedColorsEnabled = this.autoProceedWithRandomizedColors?.enabled
                ?: return null,
            isDevOptionsEnabled = isDevOptionsEnabled(),
        )
    }

    // endregion

    // region Transitions

    fun SettingsState.applyPreferredColorInputType(
        value: DomainColorInputType,
    ): Change<SettingsState, Nothing> {
        val state = this.copy(preferredColorInputType = value)
        return Change(state)
    }

    fun SettingsState.applyAppUiColorSchemeSet(
        value: DomainUiColorSchemeSet,
    ): Change<SettingsState, Nothing> {
        val state = this.copy(appUiColorSchemeSet = value)
        return Change(state)
    }

    fun SettingsState.applyDynamicUiColors(
        value: DomainDynamicUiColors,
    ): Change<SettingsState, Nothing> {
        val state = this.copy(dynamicUiColors = value)
        return Change(state)
    }

    fun SettingsState.applyResumeFromLastSearchedColor(
        value: DomainResumeFromLastSearchedColorOnStartup,
    ): Change<SettingsState, Nothing> {
        val state = this.copy(resumeFromLastSearchedColor = value)
        return Change(state)
    }

    fun SettingsState.applySmartBackspace(
        value: DomainSmartBackspace,
    ): Change<SettingsState, Nothing> {
        val state = this.copy(smartBackspace = value)
        return Change(state)
    }

    fun SettingsState.applySelectAllTextOnTextFieldFocus(
        value: DomainSelectAllTextOnTextFieldFocus,
    ): Change<SettingsState, Nothing> {
        val state = this.copy(selectAllTextOnTextFieldFocus = value)
        return Change(state)
    }

    fun SettingsState.applyAutoProceedWithRandomizedColors(
        value: DomainAutoProceedWithRandomizedColors,
    ): Change<SettingsState, Nothing> {
        val state = this.copy(autoProceedWithRandomizedColors = value)
        return Change(state)
    }

    // endregion

    @AssistedFactory
    fun interface Factory {
        fun create(): SettingsCore
    }
}