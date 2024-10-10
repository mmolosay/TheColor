package io.github.mmolosay.thecolor.presentation.settings

/**
 * Platform-agnostic data provided by ViewModel to settings View.
 */
data class SettingsData(
    val goToHome: () -> Unit,
)