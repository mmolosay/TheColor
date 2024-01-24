package io.github.mmolosay.thecolor.presentation.home.details

import androidx.compose.ui.graphics.Color
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsData.ColorInt
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsUiData.ColorSpec
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsUiData.ColorTranslation
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsUiData.ColorTranslations
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsUiData.ContentColors
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsUiData.Divider
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsUiData.ViewColorSchemeButton
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsUiData.ViewData

fun ColorDetailsUiData(
    data: ColorDetailsData,
    viewData: ViewData,
    colors: ContentColors,
): ColorDetailsUiData =
    ColorDetailsUiData(
        background = data.color.toCompose(),
        headline = Headline(data, colors),
        translations = ColorTranslations(data, viewData, colors),
        divider = Divider(colors),
        specs = ColorSpecs(data, viewData, colors),
        viewColorSchemeButtonText = ViewColorSchemeButton(data, viewData, colors),
    )

private fun Headline(
    data: ColorDetailsData,
    colors: ContentColors,
) =
    ColorDetailsUiData.Headline(
        text = data.colorName,
        color = colors.headline
    )

private fun ColorTranslations(
    data: ColorDetailsData,
    viewData: ViewData,
    colors: ContentColors,
) =
    ColorTranslations(
        hex = HexColorTranslation(data, viewData, colors),
        rgb = RgbColorTranslation(data, viewData, colors),
        hsl = HslColorTranslation(data, viewData, colors),
        hsv = HsvColorTranslation(data, viewData, colors),
        cmyk = CmykColorTranslation(data, viewData, colors),
    )

private fun HexColorTranslation(
    data: ColorDetailsData,
    viewData: ViewData,
    colors: ContentColors,
) =
    ColorTranslation.Hex(
        label = viewData.hexLabel,
        labelColor = colors.translation.label,
        value = data.hex.value,
        valueColor = colors.translation.value,
    )

private fun RgbColorTranslation(
    data: ColorDetailsData,
    viewData: ViewData,
    colors: ContentColors,
) =
    ColorTranslation.Rgb(
        label = viewData.rgbLabel,
        labelColor = colors.translation.label,
        r = data.rgb.r,
        g = data.rgb.g,
        b = data.rgb.b,
        valueColor = colors.translation.value,
    )

private fun HslColorTranslation(
    data: ColorDetailsData,
    viewData: ViewData,
    colors: ContentColors,
) =
    ColorTranslation.Hsl(
        label = viewData.hslLabel,
        labelColor = colors.translation.label,
        h = data.hsl.h,
        s = data.hsl.s,
        l = data.hsl.l,
        valueColor = colors.translation.value,
    )

private fun HsvColorTranslation(
    data: ColorDetailsData,
    viewData: ViewData,
    colors: ContentColors,
) =
    ColorTranslation.Hsv(
        label = viewData.hsvLabel,
        labelColor = colors.translation.label,
        h = data.hsv.h,
        s = data.hsv.s,
        v = data.hsv.v,
        valueColor = colors.translation.value,
    )

private fun CmykColorTranslation(
    data: ColorDetailsData,
    viewData: ViewData,
    colors: ContentColors,
) =
    ColorTranslation.Cmyk(
        label = viewData.cmykLabel,
        labelColor = colors.translation.label,
        c = data.cmyk.c,
        m = data.cmyk.m,
        y = data.cmyk.y,
        k = data.cmyk.k,
        valueColor = colors.translation.value,
    )

private fun Divider(
    colors: ContentColors,
) =
    Divider(
        color = colors.divider,
    )

private fun ColorSpecs(
    data: ColorDetailsData,
    viewData: ViewData,
    colors: ContentColors,
) =
    listOf<ColorSpec>(
        NameColorSpec(data, viewData, colors),
    ) +
        ExactMatchSpecs(data, viewData, colors)

private fun NameColorSpec(
    data: ColorDetailsData,
    viewData: ViewData,
    colors: ContentColors,
) =
    ColorSpec.Name(
        label = viewData.nameLabel,
        labelColor = colors.specs.label,
        value = data.colorName,
        valueColor = colors.specs.value,
    )

private fun ExactMatchSpecs(
    data: ColorDetailsData,
    viewData: ViewData,
    colors: ContentColors,
) =
    buildList {
        ExactMatchSpec(data, viewData, colors).also { add(it) }
        if (data.exactMatch is ColorDetailsData.ExactMatch.No) {
            ExactValueSpec(data.exactMatch, viewData, colors).also { add(it) }
            DeviationSpec(data.exactMatch, viewData, colors).also { add(it) }
        }
    }

private fun ExactMatchSpec(
    data: ColorDetailsData,
    viewData: ViewData,
    colors: ContentColors,
): ColorSpec.ExactMatch {
    val value = when (data.exactMatch) {
        is ColorDetailsData.ExactMatch.No -> viewData.exactMatchNo
        is ColorDetailsData.ExactMatch.Yes -> viewData.exactMatchYes
    }
    return ColorSpec.ExactMatch(
        label = viewData.exactMatchLabel,
        labelColor = colors.specs.label,
        value = value,
        valueColor = colors.specs.value,
    )
}

private fun ExactValueSpec(
    data: ColorDetailsData.ExactMatch.No,
    viewData: ViewData,
    colors: ContentColors,
) =
    ColorSpec.ExactValue(
        label = viewData.exactValueLabel,
        labelColor = colors.specs.label,
        iconColor = colors.specs.value,
        value = data.exactValue,
        valueColor = colors.specs.value,
        exactColor = data.exactColor.toCompose(),
        onClick = data.onExactClick,
    )

private fun DeviationSpec(
    data: ColorDetailsData.ExactMatch.No,
    viewData: ViewData,
    colors: ContentColors,
) =
    ColorSpec.Deviation(
        label = viewData.deviationLabel,
        labelColor = colors.specs.label,
        value = data.deviation,
        valueColor = colors.specs.value,
    )

private fun ViewColorSchemeButton(
    data: ColorDetailsData,
    viewData: ViewData,
    colors: ContentColors,
) =
    ViewColorSchemeButton(
        text = viewData.viewColorSchemeButtonText,
        contentColor = colors.viewColorSchemeButtonContentColor,
        onClick = data.onViewColorSchemeClick,
    )

private fun ColorInt.toCompose(): Color =
    Color(this.srgb)