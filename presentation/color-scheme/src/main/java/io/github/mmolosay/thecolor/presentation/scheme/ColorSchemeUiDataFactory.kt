package io.github.mmolosay.thecolor.presentation.scheme

import io.github.mmolosay.thecolor.domain.model.ColorScheme
import io.github.mmolosay.thecolor.presentation.impl.toCompose
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeUiData.ApplyChangesButton
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeUiData.ModeSection
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeUiData.Swatch
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeUiData.SwatchCountSection
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeUiDataComponents.OnModeSelect
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeUiDataComponents.OnSwatchCountSelect
import org.jetbrains.annotations.VisibleForTesting

fun ColorSchemeUiData(
    data: ColorSchemeData,
    strings: ColorSchemeUiStrings,
): ColorSchemeUiData =
    ColorSchemeUiData(
        swatches = Swatches(data),
        modeSection = ModeSection(data, strings),
        swatchCountSection = SwatchCountSection(data, strings),
        applyChangesButton = ApplyChangesButton(data, strings),
    )

private fun Swatches(
    data: ColorSchemeData,
): List<Swatch> =
    data.swatches.mapIndexed { index, swatch ->
        Swatch(
            color = swatch.color.toCompose(),
            useLightContentColors = swatch.isDark,  // light content on dark and vice versa
            onClick = { data.onSwatchSelect(index) },
        )
    }

private fun ModeSection(
    data: ColorSchemeData,
    strings: ColorSchemeUiStrings,
) =
    ModeSection(
        label = strings.modeLabel,
        value = data.activeMode.name(strings),
        modes = ColorScheme.Mode.entries.map { mode ->
            ModeSection.Mode(
                name = mode.name(strings),
                isSelected = (mode == data.selectedMode),
                onSelect = OnModeSelect(data, mode),
            )
        },
    )

private fun SwatchCountSection(
    data: ColorSchemeData,
    strings: ColorSchemeUiStrings,
) =
    SwatchCountSection(
        label = strings.swatchCountLabel,
        value = data.activeSwatchCount.value.toString(),
        swatchCountItems = ColorSchemeData.SwatchCount.entries.map { count ->
            SwatchCountSection.SwatchCount(
                text = count.value.toString(),
                isSelected = (count == data.selectedSwatchCount),
                onSelect = OnSwatchCountSelect(data, count),
            )
        },
    )

private fun ApplyChangesButton(
    data: ColorSchemeData,
    strings: ColorSchemeUiStrings,
): ApplyChangesButton =
    when (data.changes) {
        is ColorSchemeData.Changes.None ->
            ApplyChangesButton.Hidden
        is ColorSchemeData.Changes.Present ->
            ApplyChangesButton.Visible(
                text = strings.applyChangesButtonText,
                onClick = data.changes.applyChanges,
            )
    }

private fun ColorScheme.Mode.name(
    strings: ColorSchemeUiStrings,
): String =
    when (this) {
        ColorScheme.Mode.Monochrome -> strings.modeMonochromeName
        ColorScheme.Mode.MonochromeDark -> strings.modeMonochromeDarkName
        ColorScheme.Mode.MonochromeLight -> strings.modeMonochromeLightName
        ColorScheme.Mode.Analogic -> strings.modeAnalogicName
        ColorScheme.Mode.Complement -> strings.modeComplementName
        ColorScheme.Mode.AnalogicComplement -> strings.modeAnalogicComplementName
        ColorScheme.Mode.Triad -> strings.modeTriadName
        ColorScheme.Mode.Quad -> strings.modeQuadName
    }

/*
 * Inline created lambdas (like {}) are not equal.
 * Extracting lambda creation into a mockable component allows easy testing.
 */
@VisibleForTesting
internal object ColorSchemeUiDataComponents {

    fun OnModeSelect(
        data: ColorSchemeData,
        mode: ColorScheme.Mode,
    ): () -> Unit = {
        data.onModeSelect(mode)
    }

    fun OnSwatchCountSelect(
        data: ColorSchemeData,
        count: ColorSchemeData.SwatchCount,
    ): () -> Unit = {
        data.onSwatchCountSelect(count)
    }
}