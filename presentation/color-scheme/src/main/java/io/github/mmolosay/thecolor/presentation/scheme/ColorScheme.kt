package io.github.mmolosay.thecolor.presentation.scheme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt
import io.github.mmolosay.thecolor.presentation.design.ColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.ProvideColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.errors.ErrorMessageWithButton
import io.github.mmolosay.thecolor.presentation.errors.message
import io.github.mmolosay.thecolor.presentation.errors.rememberDefaultErrorsUiStrings
import io.github.mmolosay.thecolor.presentation.common.colorint.toCompose
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel.DataState
import io.github.mmolosay.thecolor.utils.doNothing
import io.github.mmolosay.thecolor.domain.model.ColorScheme as DomainColorScheme
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

@Composable
fun ColorScheme(
    dataState: DataState,
) {
    val context = LocalContext.current
    val strings = remember(context) { ColorSchemeUiStrings(context) }
    when (dataState) {
        is DataState.Idle ->
            doNothing() // Color Details shouldn't be visible at Home at this point
        is DataState.Loading ->
            ColorSchemeLoading()
        is DataState.Ready -> {
            ColorScheme(
                data = dataState.data,
                strings = strings,
            )
        }
        is DataState.Error ->
            Error(error = dataState.error)
    }
}

@Composable
fun ColorScheme(
    data: ColorSchemeData,
    strings: ColorSchemeUiStrings,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Swatches(
            swatches = data.swatches,
            onSwatchClick = data.onSwatchSelect,
        )

        Spacer(modifier = Modifier.height(16.dp))
        ModeSection(
            data = data,
            strings = strings,
        )

        Spacer(modifier = Modifier.height(16.dp))
        SwatchCountSection(
            data = data,
            strings = strings,
        )

        Spacer(modifier = Modifier.height(16.dp))
        ApplyChangesButton(
            modifier = Modifier.align(Alignment.End),
            changes = data.changes,
            text = strings.applyChangesButtonText,
        )
    }
}

@Composable
private fun Swatches(
    swatches: List<ColorSchemeData.Swatch>,
    onSwatchClick: (indexOfSwatch: Int) -> Unit,
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .edgeToEdge(parentTotalHorizontalPadding = 32.dp)
            .fillMaxWidth()
            .horizontalScroll(
                state = scrollState,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 48.dp), // equivalent of content padding in LazyList
            horizontalArrangement = Arrangement.spacedBy((-32).dp),
        ) {
            swatches.forEachIndexed { index, swatch ->
                Swatch(
                    swatch = swatch,
                    onClick = { onSwatchClick(index) },
                )
            }
        }
    }
}

@Composable
private fun Swatch(
    swatch: ColorSchemeData.Swatch,
    onClick: () -> Unit,
) {
    val colors = rememberContentColors(useLight = swatch.isDark) // light content on dark and vice versa
    ProvideColorsOnTintedSurface(colors) { // provides correct ripple
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(swatch.color.toCompose())
                .clickable(onClick = onClick),
        )
    }
}

private fun Modifier.edgeToEdge(
    parentTotalHorizontalPadding: Dp,
): Modifier =
    layout { measurable, constraints ->
        val expandedConstraints = constraints.copy(
            maxWidth = constraints.maxWidth + parentTotalHorizontalPadding.roundToPx(),
        )
        val placeable = measurable.measure(expandedConstraints)
        layout(placeable.width, placeable.height) {
            placeable.placeRelative(x = 0, y = 0)
        }
    }

@Composable
private fun ModeSection(
    data: ColorSchemeData,
    strings: ColorSchemeUiStrings,
) {
    SectionTitle(
        label = strings.modeLabel,
        value = data.activeMode.name(strings),
    )
    Spacer(modifier = Modifier.height(4.dp))
    Modes(
        data = data,
        strings = strings,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Modes(
    data: ColorSchemeData,
    strings: ColorSchemeUiStrings,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DomainColorScheme.Mode.entries.forEach { mode ->
            ModeChip(
                selected = (mode == data.selectedMode),
                onClick = { data.onModeSelect(mode) },
                name = mode.name(strings),
            )
        }
    }
}

@Composable
private fun ModeChip(
    selected: Boolean,
    onClick: () -> Unit,
    name: String,
) {
    SelectableChip(
        selected = selected,
        onClick = onClick,
        label = {
            SelectableChipLabel(text = name)
        },
    )
}

private fun DomainColorScheme.Mode.name(
    strings: ColorSchemeUiStrings,
): String =
    when (this) {
        DomainColorScheme.Mode.Monochrome -> strings.modeMonochromeName
        DomainColorScheme.Mode.MonochromeDark -> strings.modeMonochromeDarkName
        DomainColorScheme.Mode.MonochromeLight -> strings.modeMonochromeLightName
        DomainColorScheme.Mode.Analogic -> strings.modeAnalogicName
        DomainColorScheme.Mode.Complement -> strings.modeComplementName
        DomainColorScheme.Mode.AnalogicComplement -> strings.modeAnalogicComplementName
        DomainColorScheme.Mode.Triad -> strings.modeTriadName
        DomainColorScheme.Mode.Quad -> strings.modeQuadName
    }

@Composable
private fun SwatchCountSection(
    data: ColorSchemeData,
    strings: ColorSchemeUiStrings,
) {
    SectionTitle(
        label = strings.swatchCountLabel,
        value = data.activeSwatchCount.stringValue(),
    )
    Spacer(modifier = Modifier.height(4.dp))
    SwatchCountItems(
        data = data,
    )

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SwatchCountItems(
    data: ColorSchemeData,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ColorSchemeData.SwatchCount.entries.forEach { swatchCount ->
            SwatchCountItem(
                selected = (swatchCount == data.selectedSwatchCount),
                onClick = { data.onSwatchCountSelect(swatchCount) },
                value = swatchCount.stringValue(),
            )
        }
    }
}

@Composable
private fun SwatchCountItem(
    selected: Boolean,
    onClick: () -> Unit,
    value: String,
) {
    SelectableChip(
        selected = selected,
        onClick = onClick,
        label = {
            SelectableChipLabel(text = value)
        },
    )
}

private fun ColorSchemeData.SwatchCount.stringValue(): String =
    this.value.toString()

@Composable
private fun SectionTitle(
    label: String,
    value: String,
) {
    val labelColor = colorsOnTintedSurface.accent
    val valueColor = colorsOnTintedSurface.muted
    val text = buildAnnotatedString {
        withStyle(SpanStyle(color = labelColor)) {
            append(label)
        }
        append(' ')
        withStyle(SpanStyle(color = valueColor)) {
            append(value)
        }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectableChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
) {
    val colors = FilterChipDefaults.filterChipColors(
        containerColor = Color.Transparent,
        selectedContainerColor = colorsOnTintedSurface.muted.copy(alpha = 0.15f),
        labelColor = colorsOnTintedSurface.muted,
        selectedLabelColor = colorsOnTintedSurface.accent,
    )
    val border = FilterChipDefaults.filterChipBorder(
        enabled = true,
        selected = selected,
        borderColor = colorsOnTintedSurface.muted,
        borderWidth = 0.5.dp,
        selectedBorderWidth = 0.dp,
    )
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides Dp.Unspecified,
    ) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            shape = RoundedCornerShape(percent = 100),
            colors = colors,
            border = border,
            label = label,
        )
    }
}

@Composable
private fun SelectableChipLabel(
    text: String,
) =
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
    )

@Composable
private fun ApplyChangesButton(
    changes: ColorSchemeData.Changes,
    text: String,
    modifier: Modifier = Modifier,
) {
    // when 'changes' becomes 'Changes.None', we want to have memoized last
    // 'Changes.Present' data for some time while "exit" animation is running
    var presentChanges by remember { mutableStateOf<ColorSchemeData.Changes.Present?>(null) }
    val translation = with(LocalDensity.current) { 12.dp.roundToPx() }
    fun <T> animationSpec(): FiniteAnimationSpec<T> = spring(stiffness = 1_000f)
    AnimatedVisibility(
        visible = changes is ColorSchemeData.Changes.Present,
        modifier = modifier,
        enter = slideInHorizontally(
            animationSpec = animationSpec(),
            initialOffsetX = { translation },
        ) + fadeIn(
            animationSpec = animationSpec(),
        ),
        exit = slideOutHorizontally(
            animationSpec = animationSpec(),
            targetOffsetX = { translation },
        ) + fadeOut(
            animationSpec = animationSpec(),
        ),
    ) {
        val lastPresentChanges = presentChanges ?: return@AnimatedVisibility
        val colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colorsOnTintedSurface.accent,
        )
        val border = ButtonDefaults.outlinedButtonBorder().copy(
            brush = SolidColor(colorsOnTintedSurface.muted),
        )
        OutlinedButton(
            onClick = lastPresentChanges.applyChanges,
            colors = colors,
            border = border,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(DesignR.drawable.ic_check),
                contentDescription = null, // described by text above
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
            )
        }
    }
    LaunchedEffect(changes) {
        presentChanges = changes as? ColorSchemeData.Changes.Present ?: return@LaunchedEffect
    }
}

@Composable
private fun Error(
    error: ColorSchemeError,
) {
    val strings = rememberDefaultErrorsUiStrings()
    ErrorMessageWithButton(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        message = error.type.message(strings),
        button = {
            val colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colorsOnTintedSurface.accent,
            )
            val border = ButtonDefaults.outlinedButtonBorder().copy(
                brush = SolidColor(colorsOnTintedSurface.accent),
            )
            OutlinedButton(
                onClick = error.tryAgain,
                colors = colors,
                border = border,
            ) {
                Text(text = strings.actionTryAgain)
            }
        },
    )
}

@Composable
private fun rememberContentColors(useLight: Boolean): ColorsOnTintedSurface =
    remember(useLight) { if (useLight) colorsOnDarkSurface() else colorsOnLightSurface() }

@Preview(showBackground = true)
@Composable
private fun PreviewLight() {
    TheColorTheme {
        val colors = rememberContentColors(useLight = true)
        ProvideColorsOnTintedSurface(colors) {
            ColorScheme(
                modifier = Modifier.background(Color(0xFF_123123)),
                data = previewData(),
                strings = previewUiStrings(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDark() {
    TheColorTheme {
        val colors = rememberContentColors(useLight = false)
        ProvideColorsOnTintedSurface(colors) {
            ColorScheme(
                modifier = Modifier.background(Color(0xFF_F0F8FF)),
                data = previewData(),
                strings = previewUiStrings(),
            )
        }
    }
}

private fun previewData() =
    ColorSchemeData(
        swatches = listOf(
            ColorSchemeData.Swatch(
                color = ColorInt(0x05160B),
                isDark = true,
            ),
            ColorSchemeData.Swatch(
                color = ColorInt(0x0A2D17),
                isDark = true,
            ),
            ColorSchemeData.Swatch(
                color = ColorInt(0x0F4522),
                isDark = true,
            ),
            ColorSchemeData.Swatch(
                color = ColorInt(0x135C2E),
                isDark = true,
            ),
            ColorSchemeData.Swatch(
                color = ColorInt(0x187439),
                isDark = true,
            ),
            ColorSchemeData.Swatch(
                color = ColorInt(0x1C8C45),
                isDark = false,
            ),
            ColorSchemeData.Swatch(
                color = ColorInt(0x20A450),
                isDark = false,
            ),
            ColorSchemeData.Swatch(
                color = ColorInt(0x24BC5C),
                isDark = false,
            ),
            ColorSchemeData.Swatch(
                color = ColorInt(0x28D567),
                isDark = false,
            ),
        ),
        onSwatchSelect = {},
        activeMode = DomainColorScheme.Mode.MonochromeDark,
        selectedMode = DomainColorScheme.Mode.MonochromeDark,
        onModeSelect = {},
        activeSwatchCount = ColorSchemeData.SwatchCount.Nine,
        selectedSwatchCount = ColorSchemeData.SwatchCount.Nine,
        onSwatchCountSelect = {},
        changes = ColorSchemeData.Changes.Present(applyChanges = {}),
    )

@Suppress("SpellCheckingInspection", "RedundantSuppression")
private fun previewUiStrings() =
    ColorSchemeUiStrings(
        modeLabel = "Mode:",
        modeMonochromeName = "monochrome",
        modeMonochromeDarkName = "monochrome-dark",
        modeMonochromeLightName = "monochrome-light",
        modeAnalogicName = "analogic",
        modeComplementName = "complement",
        modeAnalogicComplementName = "analogic-complement",
        modeTriadName = "triad",
        modeQuadName = "quad",
        swatchCountLabel = "Swatch count:",
        applyChangesButtonText = "Apply changes",
    )