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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.ColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.ProvideColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.errors.ErrorMessageWithButton
import io.github.mmolosay.thecolor.presentation.errors.message
import io.github.mmolosay.thecolor.presentation.errors.rememberDefaultErrorViewData
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeUiData.ApplyChangesButton
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeUiData.ModeSection
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeUiData.Swatch
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeUiData.SwatchCountSection
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel.DataState

@Composable
fun ColorScheme(
    viewModel: ColorSchemeViewModel,
) {
    val state = viewModel.dataStateFlow.collectAsStateWithLifecycle().value
    ColorScheme(
        state = state,
    )
}

@Composable
fun ColorScheme(
    state: DataState,
) {
    val viewData = rememberViewData()
    when (state) {
        is DataState.Idle ->
            Unit // Color Details shouldn't be visible at Home at this point
        is DataState.Loading ->
            Loading()
        is DataState.Ready -> {
            val uiData = rememberUiData(state.data, viewData)
            ColorScheme(uiData)
        }
        is DataState.Error ->
            Error(error = state.error)
    }
}

@Composable
fun ColorScheme(
    uiData: ColorSchemeUiData,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Swatches(swatches = uiData.swatches)

        Spacer(modifier = Modifier.height(16.dp))
        ModeSection(uiData = uiData.modeSection)

        Spacer(modifier = Modifier.height(16.dp))
        SwatchCountSection(uiData = uiData.swatchCountSection)

        Spacer(modifier = Modifier.height(16.dp))
        ApplyChangesButton(
            uiData = uiData.applyChangesButton,
            modifier = Modifier.align(Alignment.End),
        )
    }
}

@Composable
private fun Swatches(swatches: List<Swatch>) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .edgeToEdge(parentTotalHorizontalPadding = 32.dp)
            .fillMaxWidth()
            .wrapContentWidth()
            .horizontalScroll(
                state = scrollState,
            ),
        horizontalArrangement = Arrangement.spacedBy((-32).dp),
    ) {
        swatches.forEach { swatch ->
            Swatch(swatch)
        }
    }
}

@Composable
private fun Swatch(swatch: Swatch) {
    val colors = rememberContentColors(useLight = swatch.useLightContentColors)
    ProvideColorsOnTintedSurface(colors) { // provides correct ripple
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(swatch.color)
                .clickable(onClick = swatch.onClick),
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
private fun ModeSection(uiData: ModeSection) {
    SectionTitle(
        label = uiData.label,
        value = uiData.value,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Modes(modes = uiData.modes)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Modes(modes: List<ModeSection.Mode>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        modes.forEach { mode ->
            ModeChip(mode)
        }
    }
}

@Composable
private fun ModeChip(mode: ModeSection.Mode) {
    SelectableChip(
        selected = mode.isSelected,
        onClick = mode.onSelect,
        label = {
            SelectableChipLabel(text = mode.name)
        },
    )
}

@Composable
private fun SwatchCountSection(uiData: SwatchCountSection) {
    SectionTitle(
        label = uiData.label,
        value = uiData.value,
    )
    Spacer(modifier = Modifier.height(4.dp))
    SwatchCountItems(items = uiData.swatchCountItems)

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SwatchCountItems(items: List<SwatchCountSection.SwatchCount>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { swatchCount ->
            SwatchCountItem(swatchCount)
        }
    }
}

@Composable
private fun SwatchCountItem(item: SwatchCountSection.SwatchCount) {
    SelectableChip(
        selected = item.isSelected,
        onClick = item.onSelect,
        label = {
            SelectableChipLabel(text = item.text)
        },
    )
}

@Composable
private fun SectionTitle(
    label: String,
    value: String,
) {
    val labelColor = colorsOnTintedSurface.accent
    val valueColor = colorsOnTintedSurface.muted
    val text = remember(label, value) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = labelColor)) {
                append(label)
            }
            append(' ')
            withStyle(SpanStyle(color = valueColor)) {
                append(value)
            }
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
        LocalMinimumInteractiveComponentEnforcement provides false,
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
    uiData: ApplyChangesButton,
    modifier: Modifier = Modifier,
) {
    // when uiData is Hidden, we want to have memoized Visible data for some time while "exit" animation is running
    var visibleUiData by remember { mutableStateOf<ApplyChangesButton.Visible?>(null) }
    val translation = with(LocalDensity.current) { 12.dp.roundToPx() }
    fun <T> animationSpec(): FiniteAnimationSpec<T> = spring(stiffness = 1_000f)
    AnimatedVisibility(
        visible = uiData is ApplyChangesButton.Visible,
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
        val lastVisible = visibleUiData ?: return@AnimatedVisibility
        val colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colorsOnTintedSurface.accent,
        )
        val border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = SolidColor(colorsOnTintedSurface.muted),
        )
        OutlinedButton(
            onClick = lastVisible.onClick,
            colors = colors,
            border = border,
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null, // described by text above
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = lastVisible.text,
            )
        }
    }
    LaunchedEffect(uiData) {
        visibleUiData = uiData as? ApplyChangesButton.Visible ?: return@LaunchedEffect
    }
}

@Composable
private fun Loading() =
    CircularProgressIndicator(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(),
        color = LocalContentColor.current,
    )

@Composable
private fun Error(
    error: ColorSchemeError,
) {
    val viewData = rememberDefaultErrorViewData()
    ErrorMessageWithButton(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        message = error.type.message(viewData),
        button = {
            val colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colorsOnTintedSurface.accent,
            )
            val border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = SolidColor(colorsOnTintedSurface.accent),
            )
            OutlinedButton(
                onClick = error.tryAgain,
                colors = colors,
                border = border,
            ) {
                Text(text = viewData.actionTryAgain)
            }
        },
    )
}

@Composable
private fun rememberViewData(): ColorSchemeUiData.ViewData {
    val context = LocalContext.current
    return remember { ColorSchemeViewData(context) }
}

@Composable
private fun rememberUiData(
    data: ColorSchemeData,
    viewData: ColorSchemeUiData.ViewData,
): ColorSchemeUiData =
    remember(data) { ColorSchemeUiData(data, viewData) }

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
                uiData = previewUiData(),
                modifier = Modifier.background(Color(0xFF_123123)),
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
                uiData = previewUiData(),
                modifier = Modifier.background(Color(0xFF_F0F8FF)),
            )
        }
    }
}

@Suppress("MoveLambdaOutsideParentheses")
private fun previewUiData() =
    ColorSchemeUiData(
        swatches = listOf(
            Swatch(
                color = Color(0xFF_05160B),
                useLightContentColors = true,
                onClick = {},
            ),
            Swatch(
                color = Color(0xFF_0A2D17),
                useLightContentColors = true,
                onClick = {},
            ),
            Swatch(
                color = Color(0xFF_0F4522),
                useLightContentColors = true,
                onClick = {},
            ),
            Swatch(
                color = Color(0xFF_135C2E),
                useLightContentColors = true,
                onClick = {},
            ),
            Swatch(
                color = Color(0xFF_187439),
                useLightContentColors = true,
                onClick = {},
            ),
            Swatch(
                color = Color(0xFF_1C8C45),
                useLightContentColors = false,
                onClick = {},
            ),
            Swatch(
                color = Color(0xFF_20A450),
                useLightContentColors = false,
                onClick = {},
            ),
            Swatch(
                color = Color(0xFF_24BC5C),
                useLightContentColors = false,
                onClick = {},
            ),
            Swatch(
                color = Color(0xFF_28D567),
                useLightContentColors = false,
                onClick = {},
            ),
        ),
        modeSection = ModeSection(
            label = "Mode:",
            value = "analogic-complement",
            modes = listOf(
                ModeSection.Mode("monochrome", false, {}),
                ModeSection.Mode("monochrome-dark", false, {}),
                ModeSection.Mode("monochrome-light", false, {}),
                ModeSection.Mode("analogic", isSelected = true, {}),
                ModeSection.Mode("complement", false, {}),
                ModeSection.Mode("analogic-complement", false, {}),
                ModeSection.Mode("triad", false, {}),
                ModeSection.Mode("quad", false, {}),
            ),
        ),
        swatchCountSection = SwatchCountSection(
            label = "Swatch count:",
            value = "12",
            swatchCountItems = listOf(
                SwatchCountSection.SwatchCount("5", false, {}),
                SwatchCountSection.SwatchCount("6", false, {}),
                SwatchCountSection.SwatchCount("8", false, {}),
                SwatchCountSection.SwatchCount("10", isSelected = true, {}),
                SwatchCountSection.SwatchCount("12", false, {}),
                SwatchCountSection.SwatchCount("15", false, {}),
            ),
        ),
        applyChangesButton = ApplyChangesButton.Visible(
            text = "Apply changes",
            onClick = {},
        ),
    )