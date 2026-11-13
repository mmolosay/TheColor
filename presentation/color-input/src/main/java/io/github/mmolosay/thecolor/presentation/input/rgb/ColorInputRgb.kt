package io.github.mmolosay.thecolor.presentation.input.rgb

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.common.compose.smartBackspace
import io.github.mmolosay.thecolor.presentation.common.compose.thenIf
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.UiComponents.ProcessSubmissionAnswerAsSideEffect
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmission
import io.github.mmolosay.thecolor.presentation.input.textfield.TextField
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldUiStrings
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldViewData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@Composable
fun ColorInputRgb(
    feature: ColorInputRgbFeature,
    strings: ColorInputRgbUiStrings = rememberColorInputRgbUiStrings(),
) =
    ColorInputRgb(
        viewData = feature.viewData,
        onSubmit = feature::submit,
        onAckAnswer = feature::ackAnswer,
        rViewData = feature.r.viewData,
        onREnterText = feature.r::enterText,
        gViewData = feature.g.viewData,
        onGEnterText = feature.g::enterText,
        bViewData = feature.b.viewData,
        onBEnterText = feature.b::enterText,
        strings = strings,
    )

@Composable
fun ColorInputRgb(
    viewData: StateFlow<ColorInputRgbViewData>,
    onSubmit: () -> Unit,
    onAckAnswer: (ColorInputSubmission.Answered) -> Unit,
    rViewData: StateFlow<TextFieldViewData>,
    onREnterText: (text: String, expected: String) -> Unit,
    gViewData: StateFlow<TextFieldViewData>,
    onGEnterText: (text: String, expected: String) -> Unit,
    bViewData: StateFlow<TextFieldViewData>,
    onBEnterText: (text: String, expected: String) -> Unit,
    strings: ColorInputRgbUiStrings,
) {
    val data = viewData.collectAsStateWithLifecycle().value

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val modifier = Modifier.weight(1f)
        val isSmartBackspaceEnabled = data.isSmartBackspaceEnabled

        // R
        ComponentAdvancedTextField(
            modifier = modifier,
            viewData = rViewData,
            onEnterText = onREnterText,
            imeAction = ImeAction.Next,
            onKeyboardAction = null,
            strings = strings.rTextField,
            hasPreviousComponent = false,
            enableSmartBackspace = isSmartBackspaceEnabled,
        )

        // G
        ComponentAdvancedTextField(
            modifier = modifier,
            viewData = gViewData,
            onEnterText = onGEnterText,
            imeAction = ImeAction.Next,
            onKeyboardAction = null,
            strings = strings.gTextField,
            hasPreviousComponent = true,
            enableSmartBackspace = isSmartBackspaceEnabled,
        )

        // B
        ComponentAdvancedTextField(
            modifier = modifier,
            viewData = bViewData,
            onEnterText = onBEnterText,
            imeAction = ImeAction.Done,
            onKeyboardAction = { onSubmit() },
            strings = strings.bTextField,
            hasPreviousComponent = true,
            enableSmartBackspace = isSmartBackspaceEnabled,
        )
    }

    ProcessSubmissionAnswerAsSideEffect(
        submission = data.answeredSubmission,
        rejectionMessage = strings.invalidSubmittedColorMessage,
        onProcessed = onAckAnswer,
    )
}

/**
 * A wrapper for [ComponentBasicTextField] with additional features, such as "smart backspace".
 */
@Composable
private fun ComponentAdvancedTextField(
    modifier: Modifier = Modifier,
    viewData: StateFlow<TextFieldViewData>,
    onEnterText: (text: String, expected: String) -> Unit,
    imeAction: ImeAction,
    onKeyboardAction: KeyboardActionHandler?,
    strings: TextFieldUiStrings,
    hasPreviousComponent: Boolean,
    enableSmartBackspace: Boolean,
) {
    ComponentBasicTextField(
        modifier = modifier
            .thenIf(enableSmartBackspace && hasPreviousComponent) {
                smartBackspace(text = { viewData.value.text })
            },
        viewData = viewData,
        onEnterText = onEnterText,
        imeAction = imeAction,
        onKeyboardAction = onKeyboardAction,
        strings = strings,
    )
}

/**
 * A wrapper for a [TextField] that is configured to be an RGB component.
 */
@Composable
private fun ComponentBasicTextField(
    modifier: Modifier = Modifier,
    viewData: StateFlow<TextFieldViewData>,
    onEnterText: (text: String, expected: String) -> Unit,
    imeAction: ImeAction,
    onKeyboardAction: KeyboardActionHandler?,
    strings: TextFieldUiStrings,
) {
    TextField(
        modifier = modifier,
        viewData = viewData,
        onEnterText = onEnterText,
        onClearText = null,
        strings = strings,
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction,
            keyboardType = KeyboardType.Number,
        ),
        onKeyboardAction = onKeyboardAction,
    )
}

@Composable
private fun rememberColorInputRgbUiStrings(): ColorInputRgbUiStrings {
    val context = LocalContext.current
    return remember(context) { ColorInputRgbUiStrings(context) }
}

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    val viewData = remember {
        val value = ColorInputRgbViewData(
            answeredSubmission = null,
            isSmartBackspaceEnabled = true,
        )
        MutableStateFlow(value)
    }
    val rViewData = remember {
        val value = TextFieldViewData(
            text = "12",
            version = 0,
            selectAllTextOnFocus = true,
        )
        MutableStateFlow(value)
    }
    val gViewData = remember {
        val value = TextFieldViewData(
            text = "",
            version = 0,
            selectAllTextOnFocus = true,
        )
        MutableStateFlow(value)
    }
    val bViewData = remember {
        val value = TextFieldViewData(
            text = "255",
            version = 0,
            selectAllTextOnFocus = true,
        )
        MutableStateFlow(value)
    }
    TheColorTheme {
        ColorInputRgb(
            viewData = viewData,
            onSubmit = {},
            onAckAnswer = {},
            rViewData = rViewData,
            onREnterText = { text, _ ->
                rViewData.update {
                    it.copy(text = text, version = it.version + 1)
                }
            },
            gViewData = gViewData,
            onGEnterText = { text, _ ->
                gViewData.update {
                    it.copy(text = text, version = it.version + 1)
                }
            },
            bViewData = bViewData,
            onBEnterText = { text, _ ->
                bViewData.update {
                    it.copy(text = text, version = it.version + 1)
                }
            },
            strings = previewUiStrings(),
        )
    }
}

private fun previewUiStrings() =
    ColorInputRgbUiStrings(
        rTextField = TextFieldUiStrings(
            label = "R",
            placeholder = "0",
            prefix = null,
            trailingIconContentDesc = null,
        ),
        gTextField = TextFieldUiStrings(
            label = "G",
            placeholder = "0",
            prefix = null,
            trailingIconContentDesc = null,
        ),
        bTextField = TextFieldUiStrings(
            label = "B",
            placeholder = "0",
            prefix = null,
            trailingIconContentDesc = null,
        ),
        invalidSubmittedColorMessage = "Please enter a valid color",
    )