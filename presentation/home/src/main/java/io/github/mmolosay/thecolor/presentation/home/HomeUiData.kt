package io.github.mmolosay.thecolor.presentation.home

import androidx.compose.ui.graphics.Color
import io.github.mmolosay.thecolor.presentation.api.ColorInt
import io.github.mmolosay.thecolor.presentation.api.NavBarAppearance

/**
 * Framework-oriented data required for Home screen to be presented by Compose.
 * It's a derivative from [HomeData].
 */
data class HomeUiData(
    val topBar: TopBar,
    val headline: String,
    val proceedButton: ProceedButton,
    val colorPreviewState: ColorPreviewState,
    val showColorCenter: ShowColorCenter,
    val invalidSubmittedColorToast: InvalidSubmittedColorToast?,
) {

    data class TopBar(
        val settingsAction: SettingsAction,
    ) {
        data class SettingsAction(
            val onClick: () -> Unit,
            val iconContentDescription: String,
        )
    }

    data class ProceedButton(
        val onClick: () -> Unit,
        val enabled: Boolean,
        val text: String,
    )

    enum class ColorPreviewState {
        Default, Submitted,
    }

    // sealed interface with 2 options vs. nullable data class are two ways of implementing duality in UI model
    sealed interface ShowColorCenter {
        data object No : ShowColorCenter
        data class Yes(
            val backgroundColor: Color,
            val useLightContentColors: Boolean,
            val navBarAppearance: NavBarAppearance,
        ) : ShowColorCenter
    }

    // sealed interface with 2 options vs. nullable data class are two ways of implementing duality in UI model
    data class InvalidSubmittedColorToast(
        val message: String,
        val onShown: () -> Unit,
    )
}