package io.github.mmolosay.thecolor.presentation.scheme

import android.content.Context

/**
 * Strings that are pre-defined in UI and don't come from ViewModel.
 *
 * This object is created by View, since string resources are tied to platform-specific
 * components (like Context), which should be avoided in ViewModels.
 */
data class ColorSchemeUiStrings(
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

fun ColorSchemeUiStrings(context: Context) =
    ColorSchemeUiStrings(
        modeLabel = context.getString(R.string.color_scheme_mode_label),
        modeMonochromeName = context.getString(R.string.color_scheme_mode_name_monochrome),
        modeMonochromeDarkName = context.getString(R.string.color_scheme_mode_name_monochrome_dark),
        modeMonochromeLightName = context.getString(R.string.color_scheme_mode_name_monochrome_light),
        modeAnalogicName = context.getString(R.string.color_scheme_mode_name_analogic),
        modeComplementName = context.getString(R.string.color_scheme_mode_name_complement),
        modeAnalogicComplementName = context.getString(R.string.color_scheme_mode_name_analogic_complement),
        modeTriadName = context.getString(R.string.color_scheme_mode_name_triad),
        modeQuadName = context.getString(R.string.color_scheme_mode_name_quad),
        swatchCountLabel = context.getString(R.string.color_scheme_swatch_count_label),
        applyChangesButtonText = context.getString(R.string.color_scheme_apply_changes_button_text),
    )