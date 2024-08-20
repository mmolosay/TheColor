package io.github.mmolosay.thecolor.presentation.scheme

import io.github.mmolosay.thecolor.domain.model.ColorScheme
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeUiData.ApplyChangesButton
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeUiData.ModeSection
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeUiData.Swatch
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeUiData.SwatchCountSection
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeUiDataComponents.OnModeSelect
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeUiDataComponents.OnSwatchClick
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeUiDataComponents.OnSwatchCountSelect
import io.github.mmolosay.thecolor.presentation.impl.toCompose
import org.jetbrains.annotations.VisibleForTesting

fun ColorSchemeUiData(
    data: ColorSchemeData,
    viewData: ColorSchemeUiData.ViewData,
): ColorSchemeUiData =
    ColorSchemeUiData(
        swatches = Swatches(data),
        modeSection = ModeSection(data, viewData),
        swatchCountSection = SwatchCountSection(data, viewData),
        applyChangesButton = ApplyChangesButton(data, viewData),
    )

private fun Swatches(
    data: ColorSchemeData,
): List<Swatch> =
    data.swatches.mapIndexed { index, swatch ->
        Swatch(
            color = swatch.color.toCompose(),
            useLightContentColors = swatch.isDark,  // light content on dark and vice versa
            onClick = OnSwatchClick(data, index),
        )
    }

private fun ModeSection(
    data: ColorSchemeData,
    viewData: ColorSchemeUiData.ViewData,
) =
    ModeSection(
        label = viewData.modeLabel,
        value = data.activeMode.name(viewData),
        modes = ColorScheme.Mode.entries.map { mode ->
            ModeSection.Mode(
                name = mode.name(viewData),
                isSelected = (mode == data.selectedMode),
                onSelect = OnModeSelect(data, mode),
            )
        },
    )

private fun SwatchCountSection(
    data: ColorSchemeData,
    viewData: ColorSchemeUiData.ViewData,
) =
    SwatchCountSection(
        label = viewData.swatchCountLabel,
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
    viewData: ColorSchemeUiData.ViewData,
): ApplyChangesButton =
    when (data.changes) {
        is ColorSchemeData.Changes.None ->
            ApplyChangesButton.Hidden
        is ColorSchemeData.Changes.Present ->
            ApplyChangesButton.Visible(
                text = viewData.applyChangesButtonText,
                onClick = data.changes.applyChanges,
            )
    }

private fun ColorScheme.Mode.name(
    viewData: ColorSchemeUiData.ViewData,
): String =
    when (this) {
        ColorScheme.Mode.Monochrome -> viewData.modeMonochromeName
        ColorScheme.Mode.MonochromeDark -> viewData.modeMonochromeDarkName
        ColorScheme.Mode.MonochromeLight -> viewData.modeMonochromeLightName
        ColorScheme.Mode.Analogic -> viewData.modeAnalogicName
        ColorScheme.Mode.Complement -> viewData.modeComplementName
        ColorScheme.Mode.AnalogicComplement -> viewData.modeAnalogicComplementName
        ColorScheme.Mode.Triad -> viewData.modeTriadName
        ColorScheme.Mode.Quad -> viewData.modeQuadName
    }

/*
 * Inline created lambdas (like {}) are not equal.
 * Extracting lambda creation into a mockable component allows easy testing.
 */
@VisibleForTesting
internal object ColorSchemeUiDataComponents {

    fun OnSwatchClick(
        data: ColorSchemeData,
        swatchIndex: Int,
    ): () -> Unit = {
        data.onSwatchSelect(swatchIndex)
    }

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