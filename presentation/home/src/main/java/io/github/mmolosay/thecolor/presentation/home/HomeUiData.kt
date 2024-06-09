package io.github.mmolosay.thecolor.presentation.home

import android.content.Context
import androidx.compose.ui.graphics.Color

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

    sealed interface ShowColorCenter {
        data object No : ShowColorCenter
        data class Yes(
            val backgroundColor: Color,
            val useLightContentColors: Boolean,
        ) : ShowColorCenter
    }

    /**
     * Part of to-be [HomeUiData].
     * Framework-oriented.
     * Created by View, since string resources are tied to platform-specific
     * components (like Context), which should be avoided in ViewModels.
     */
    data class ViewData(
        val settingsIconContentDesc: String,
        val headline: String,
        val proceedButtonText: String,
    )
}

fun HomeViewData(context: Context) =
    HomeUiData.ViewData(
        settingsIconContentDesc = context.getString(R.string.home_settings_icon_content_desc),
        headline = context.getString(R.string.home_headline),
        proceedButtonText = context.getString(R.string.home_proceed_btn),
    )