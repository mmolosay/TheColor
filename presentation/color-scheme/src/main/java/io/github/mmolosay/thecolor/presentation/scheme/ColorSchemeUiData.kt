package io.github.mmolosay.thecolor.presentation.scheme

import androidx.compose.ui.graphics.Color
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeUiData.ViewData

/**
 * Framework-oriented data required for color scheme View to be presented by Compose.
 * It is a combination of [ColorSchemeData] and [ViewData].
 */
data class ColorSchemeUiData(
    val swatches: List<Color>,
    val modeSection: ModeSection,
    val swatchCountSection: SwatchCountSection,
    val applyChangesButton: ApplyChangesButton,
) {

    data class ModeSection(
        val label: String,
        val value: String,
        val modes: List<Mode>,
    ) {
        data class Mode(
            val name: String,
            val isSelected: Boolean,
            val onSelect: () -> Unit,
        )
    }

    data class SwatchCountSection(
        val label: String,
        val value: String,
        val swatchCountItems: List<SwatchCount>,
    ) {
        data class SwatchCount(
            val text: String,
            val isSelected: Boolean,
            val onSelect: () -> Unit,
        )
    }

    sealed interface ApplyChangesButton {
        data object Hidden : ApplyChangesButton
        data class Visible(
            val text: String,
            val onClick: () -> Unit,
        ) : ApplyChangesButton
    }

    /**
     * Part of to-be [ColorSchemeUiData].
     * Framework-oriented.
     *
     * Created by View, since string resources are tied to platform-specific
     * components (like Context), which should be avoided in ViewModels.
     */
    data class ViewData(
        val modeLabel: String,
        val modeMonochromeName: String,
        val modeMonochromeDarkName: String,
        val modeMonochromeLightName: String,
        val modeAnalogicName: String,
        val modeComplementName: String,
        val modeAnalogicComplementName: String,
        val modeTriadName: String,
        val modeQuadName: String,
        val swatchCountLabel: String,
        val applyChangesButtonText: String,
    )
}