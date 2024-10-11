package io.github.mmolosay.thecolor.presentation.details

import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.ColorSpec
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.ColorTranslation
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.ColorTranslations
import io.github.mmolosay.thecolor.presentation.impl.toCompose

internal fun ColorDetailsUiData(
    data: ColorDetailsData,
    strings: ColorDetailsUiStrings,
): ColorDetailsUiData =
    ColorDetailsUiData(
        headline = data.colorName,
        translations = ColorTranslations(data, strings),
        specs = ColorSpecs(data, strings),
    )

private fun ColorTranslations(
    data: ColorDetailsData,
    strings: ColorDetailsUiStrings,
) =
    ColorTranslations(
        hex = HexColorTranslation(data, strings),
        rgb = RgbColorTranslation(data, strings),
        hsl = HslColorTranslation(data, strings),
        hsv = HsvColorTranslation(data, strings),
        cmyk = CmykColorTranslation(data, strings),
    )

private fun HexColorTranslation(
    data: ColorDetailsData,
    strings: ColorDetailsUiStrings,
) =
    ColorTranslation.Hex(
        label = strings.hexLabel,
        value = data.hex.value,
    )

private fun RgbColorTranslation(
    data: ColorDetailsData,
    strings: ColorDetailsUiStrings,
) =
    ColorTranslation.Rgb(
        label = strings.rgbLabel,
        r = data.rgb.r,
        g = data.rgb.g,
        b = data.rgb.b,
    )

private fun HslColorTranslation(
    data: ColorDetailsData,
    strings: ColorDetailsUiStrings,
) =
    ColorTranslation.Hsl(
        label = strings.hslLabel,
        h = data.hsl.h,
        s = data.hsl.s,
        l = data.hsl.l,
    )

private fun HsvColorTranslation(
    data: ColorDetailsData,
    strings: ColorDetailsUiStrings,
) =
    ColorTranslation.Hsv(
        label = strings.hsvLabel,
        h = data.hsv.h,
        s = data.hsv.s,
        v = data.hsv.v,
    )

private fun CmykColorTranslation(
    data: ColorDetailsData,
    strings: ColorDetailsUiStrings,
) =
    ColorTranslation.Cmyk(
        label = strings.cmykLabel,
        c = data.cmyk.c,
        m = data.cmyk.m,
        y = data.cmyk.y,
        k = data.cmyk.k,
    )

private fun ColorSpecs(
    data: ColorDetailsData,
    strings: ColorDetailsUiStrings,
) =
    buildList {
        NameColorSpec(data, strings).also { add(it) }
        ExactMatchSpecs(data, strings).also { addAll(it) }
    }

private fun NameColorSpec(
    data: ColorDetailsData,
    strings: ColorDetailsUiStrings,
) =
    ColorSpec.Name(
        label = strings.nameLabel,
        value = data.colorName,
    )

private fun ExactMatchSpecs(
    data: ColorDetailsData,
    strings: ColorDetailsUiStrings,
) =
    buildList {
        ExactMatchSpec(data, strings).also { add(it) }
        if (data.exactMatch is ColorDetailsData.ExactMatch.No) {
            ExactValueSpec(data.exactMatch, strings).also { add(it) }
            DeviationSpec(data.exactMatch, strings).also { add(it) }
        }
    }

private fun ExactMatchSpec(
    data: ColorDetailsData,
    strings: ColorDetailsUiStrings,
): ColorSpec.ExactMatch {
    val value = when (data.exactMatch) {
        is ColorDetailsData.ExactMatch.No -> strings.exactMatchNo
        is ColorDetailsData.ExactMatch.Yes -> strings.exactMatchYes
    }
    val goBackToInitialColorButton = if (data.initialColorData != null) {
        ColorSpec.ExactMatch.GoBackToInitialColorButton(
            text = strings.goBackToInitialColorButtonText,
            initialColor = data.initialColorData.initialColor.toCompose(),
            onClick = data.initialColorData.goToInitialColor,
        )
    } else {
        null
    }
    return ColorSpec.ExactMatch(
        label = strings.exactMatchLabel,
        value = value,
        goBackToInitialColorButton = goBackToInitialColorButton,
    )
}

private fun ExactValueSpec(
    data: ColorDetailsData.ExactMatch.No,
    strings: ColorDetailsUiStrings,
) =
    ColorSpec.ExactValue(
        label = strings.exactValueLabel,
        value = data.exactValue,
        exactColor = data.exactColor.toCompose(),
        onClick = data.goToExactColor,
    )

private fun DeviationSpec(
    data: ColorDetailsData.ExactMatch.No,
    strings: ColorDetailsUiStrings,
) =
    ColorSpec.Deviation(
        label = strings.deviationLabel,
        value = data.deviation,
    )