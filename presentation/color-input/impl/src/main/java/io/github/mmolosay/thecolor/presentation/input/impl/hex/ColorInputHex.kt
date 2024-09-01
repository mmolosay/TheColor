package io.github.mmolosay.thecolor.presentation.input.impl.hex

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.impl.UiComponents.Loading
import io.github.mmolosay.thecolor.presentation.input.impl.UiComponents.TextField
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldUiData
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldUiData.TrailingButton
import io.github.mmolosay.thecolor.presentation.input.impl.hex.ColorInputHexUiData.ViewData
import io.github.mmolosay.thecolor.presentation.input.impl.model.ColorInputUiCommand
import io.github.mmolosay.thecolor.presentation.input.impl.model.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@Composable
fun ColorInputHex(
    viewModel: ColorInputHexViewModel,
) {
    val viewData = rememberViewData()
    val state = viewModel.dataStateFlow.collectAsStateWithLifecycle().value
    val collectedStates = remember {
        mutableListOf<DataState<ColorInputHexData>>()
    }
    val uiCommandFlow = remember(viewModel.dataStateFlow) {
        MutableSharedFlow<ColorInputUiCommand>()
    }
    when (state) {
        is DataState.BeingInitialized ->
            Loading()
        is DataState.Ready -> {
            val uiData = rememberUiData(data = state.data, viewData = viewData)
            ColorInputHex(
                uiData = uiData,
                uiCommandFlow = uiCommandFlow,
            )
        }
    }
    LaunchedEffect(state) {
        val current = state
        val previous = collectedStates.lastOrNull()
        val newCommands = ColorInputHexUiCommands(current, previous)
        newCommands.forEach { command ->
            uiCommandFlow.emit(command)
        }
        collectedStates += current
        run dropOldStates@{
            val numberOfMaxRetainedStates = 2
            if (collectedStates.size > numberOfMaxRetainedStates) {
                collectedStates
                    .subList(0, collectedStates.size - numberOfMaxRetainedStates)
                    .clear()
            }
        }
    }
}

@Composable
fun ColorInputHex(
    uiData: ColorInputHexUiData,
    uiCommandFlow: Flow<ColorInputUiCommand>,
) {
    var value by remember { mutableStateOf(TextFieldValue(text = uiData.textField.text.string)) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    TextField(
        modifier = Modifier.fillMaxWidth(0.5f),
        uiData = uiData.textField,
        value = value,
        updateValue = { new -> value = new },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            capitalization = KeyboardCapitalization.Characters,
        ),
        keyboardActions = KeyboardActions(
            onDone = { uiData.onImeActionDone() },
        ),
    )

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            uiCommandFlow.collect { command ->
                when (command) {
                    is ColorInputUiCommand.HideSoftwareKeyboard -> {
                        keyboardController?.hide()
                    }
                }
                command.onExecuted()
            }
        }
    }
}

@Composable
private fun rememberViewData(): ViewData {
    val context = LocalContext.current
    return remember { ColorInputHexViewData(context) }
}

@Composable
private fun rememberUiData(
    data: ColorInputHexData,
    viewData: ViewData,
): ColorInputHexUiData =
    remember(data) { data + viewData } // TODO: do in background?

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TheColorTheme {
        ColorInputHex(
            uiData = previewUiData(),
            uiCommandFlow = emptyFlow(),
        )
    }
}

private fun previewUiData() =
    ColorInputHexUiData(
        textField = TextFieldUiData(
            text = Text(""),
            onTextChange = {},
            filterUserInput = { Text(it) },
            label = "HEX",
            placeholder = "000000",
            prefix = "#",
            trailingButton = TrailingButton.Visible(onClick = {}, iconContentDesc = ""),
        ),
        onImeActionDone = {},
    )