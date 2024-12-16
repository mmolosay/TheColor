package io.github.mmolosay.thecolor.presentation.scheme

import androidx.compose.ui.graphics.Color

/**
 * Framework-oriented data required for color scheme View to be presented by Compose.
 * It is a combination of [ColorSchemeData] and [ColorSchemeUiStrings].
 */
data class ColorSchemeUiData(
    val swatches: List<Swatch>,
    val modeSection: ModeSection,
    val swatchCountSection: SwatchCountSection,
    val applyChangesButton: ApplyChangesButton,
) {

    data class Swatch(
        val color: Color,
        val useLightContentColors: Boolean,
        val onClick: () -> Unit,
    )

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
}