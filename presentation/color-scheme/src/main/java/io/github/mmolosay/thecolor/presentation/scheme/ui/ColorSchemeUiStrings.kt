package io.github.mmolosay.thecolor.presentation.scheme.ui

import android.content.Context
import android.text.Spanned
import io.github.mmolosay.thecolor.presentation.scheme.R

/**
 * Strings that are pre-defined in UI and don't come from ViewModel.
 *
 * This object is created by View, since string resources are tied to platform-specific
 * components (like Context), which should be avoided in ViewModels.
 */
data class ColorSchemeUiStrings(
    val modeTitle: Spanned,
    val modeMonochromeName: String,
    val modeMonochromeDarkName: String,
    val modeMonochromeLightName: String,
    val modeAnalogicName: String,
    val modeComplementName: String,
    val modeAnalogicComplementName: String,
    val modeTriadName: String,
    val modeQuadName: String,
    val swatchCountTitle: Spanned,
    val applyChangesButtonText: String,
)

fun ColorSchemeUiStrings(context: Context) =
    ColorSchemeUiStrings(
        modeTitle = context.getText(R.string.color_scheme_mode_title) as Spanned,
        modeMonochromeName = context.getString(R.string.color_scheme_mode_name_monochrome),
        modeMonochromeDarkName = context.getString(R.string.color_scheme_mode_name_monochrome_dark),
        modeMonochromeLightName = context.getString(R.string.color_scheme_mode_name_monochrome_light),
        modeAnalogicName = context.getString(R.string.color_scheme_mode_name_analogic),
        modeComplementName = context.getString(R.string.color_scheme_mode_name_complement),
        modeAnalogicComplementName = context.getString(R.string.color_scheme_mode_name_analogic_complement),
        modeTriadName = context.getString(R.string.color_scheme_mode_name_triad),
        modeQuadName = context.getString(R.string.color_scheme_mode_name_quad),
        swatchCountTitle = context.getText(R.string.color_scheme_swatch_count_title) as Spanned,
        applyChangesButtonText = context.getString(R.string.color_scheme_apply_changes_button_text),
    )