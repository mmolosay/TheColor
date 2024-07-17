package io.github.mmolosay.thecolor.presentation.details

import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.ColorSpec
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.ColorTranslation
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.ColorTranslations
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.ViewData
import io.github.mmolosay.thecolor.presentation.toCompose

internal fun ColorDetailsUiData(
    data: ColorDetailsData,
    viewData: ViewData,
): ColorDetailsUiData =
    ColorDetailsUiData(
        headline = data.colorName,
        translations = ColorTranslations(data, viewData),
        specs = ColorSpecs(data, viewData),
    )

private fun ColorTranslations(
    data: ColorDetailsData,
    viewData: ViewData,
) =
    ColorTranslations(
        hex = HexColorTranslation(data, viewData),
        rgb = RgbColorTranslation(data, viewData),
        hsl = HslColorTranslation(data, viewData),
        hsv = HsvColorTranslation(data, viewData),
        cmyk = CmykColorTranslation(data, viewData),
    )

private fun HexColorTranslation(
    data: ColorDetailsData,
    viewData: ViewData,
) =
    ColorTranslation.Hex(
        label = viewData.hexLabel,
        value = data.hex.value,
    )

private fun RgbColorTranslation(
    data: ColorDetailsData,
    viewData: ViewData,
) =
    ColorTranslation.Rgb(
        label = viewData.rgbLabel,
        r = data.rgb.r,
        g = data.rgb.g,
        b = data.rgb.b,
    )

private fun HslColorTranslation(
    data: ColorDetailsData,
    viewData: ViewData,
) =
    ColorTranslation.Hsl(
        label = viewData.hslLabel,
        h = data.hsl.h,
        s = data.hsl.s,
        l = data.hsl.l,
    )

private fun HsvColorTranslation(
    data: ColorDetailsData,
    viewData: ViewData,
) =
    ColorTranslation.Hsv(
        label = viewData.hsvLabel,
        h = data.hsv.h,
        s = data.hsv.s,
        v = data.hsv.v,
    )

private fun CmykColorTranslation(
    data: ColorDetailsData,
    viewData: ViewData,
) =
    ColorTranslation.Cmyk(
        label = viewData.cmykLabel,
        c = data.cmyk.c,
        m = data.cmyk.m,
        y = data.cmyk.y,
        k = data.cmyk.k,
    )

private fun ColorSpecs(
    data: ColorDetailsData,
    viewData: ViewData,
) =
    buildList {
        NameColorSpec(data, viewData).also { add(it) }
        ExactMatchSpecs(data, viewData).also { addAll(it) }
    }

private fun NameColorSpec(
    data: ColorDetailsData,
    viewData: ViewData,
) =
    ColorSpec.Name(
        label = viewData.nameLabel,
        value = data.colorName,
    )

private fun ExactMatchSpecs(
    data: ColorDetailsData,
    viewData: ViewData,
) =
    buildList {
        ExactMatchSpec(data, viewData).also { add(it) }
        if (data.exactMatch is ColorDetailsData.ExactMatch.No) {
            ExactValueSpec(data.exactMatch, viewData).also { add(it) }
            DeviationSpec(data.exactMatch, viewData).also { add(it) }
        }
    }

private fun ExactMatchSpec(
    data: ColorDetailsData,
    viewData: ViewData,
): ColorSpec.ExactMatch {
    val value = when (data.exactMatch) {
        is ColorDetailsData.ExactMatch.No -> viewData.exactMatchNo
        is ColorDetailsData.ExactMatch.Yes -> viewData.exactMatchYes
    }
    val goBackToInitialColorButton = if (data.initialColorData != null) {
        ColorSpec.ExactMatch.GoBackToInitialColorButton(
            text = viewData.goBackToInitialColorButtonText,
            initialColor = data.initialColorData.initialColor.toCompose(),
            onClick = data.initialColorData.goToInitialColor,
        )
    } else {
        null
    }
    return ColorSpec.ExactMatch(
        label = viewData.exactMatchLabel,
        value = value,
        goBackToInitialColorButton = goBackToInitialColorButton,
    )
}

private fun ExactValueSpec(
    data: ColorDetailsData.ExactMatch.No,
    viewData: ViewData,
) =
    ColorSpec.ExactValue(
        label = viewData.exactValueLabel,
        value = data.exactValue,
        exactColor = data.exactColor.toCompose(),
        onClick = data.goToExactColor,
    )

private fun DeviationSpec(
    data: ColorDetailsData.ExactMatch.No,
    viewData: ViewData,
) =
    ColorSpec.Deviation(
        label = viewData.deviationLabel,
        value = data.deviation,
    )