package io.github.mmolosay.thecolor.presentation.home.input.field

import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData.Text
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData.TrailingButton
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData.ViewData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Not a ViewModel-ViewModel in terms of Android development.
 * It doesn't derive from [androidx.lifecycle.ViewModel], so should only be used in "real" ViewModels
 * which do derive from Android-aware implementation.
 */
class ColorInputFieldViewModel(
    private val viewData: ViewData,
    private val filterUserInput: (String) -> Text,
) {

    private val _uiDataFlow = MutableStateFlow(makeInitialUiData())
    val uiDataFlow = _uiDataFlow.asStateFlow()

    private fun updateText(text: Text) {
        _uiDataFlow.update {
            it.smartCopy(text = text)
        }
    }

    private fun clearText() {
        _uiDataFlow.update {
            it.smartCopy(text = Text(""))
        }
    }

    // seems like a better solution than "uiDataFlow = _uiDataFlow.map {..}"
    private fun ColorInputFieldUiData.smartCopy(
        text: Text,
    ) =
        copy(
            text = text,
            trailingButton = trailingButton(
                text = text,
                trailingIcon = viewData.trailingIcon,
            ),
        )

    private fun trailingButton(
        text: Text,
        trailingIcon: ViewData.TrailingIcon,
    ): TrailingButton =
        if (trailingIcon is ViewData.TrailingIcon.Exists && showTrailingButton(text)) {
            TrailingButton.Visible(
                onClick = ::clearText,
                iconContentDesc = trailingIcon.contentDesc,
            )
        } else {
            TrailingButton.Hidden
        }

    private fun showTrailingButton(text: Text): Boolean =
        text.string.isNotEmpty()

    private fun makeInitialUiData() =
        ColorInputFieldUiData(
            text = Text(""),
            onTextChange = ::updateText,
            filterUserInput = filterUserInput,
            label = viewData.label,
            placeholder = viewData.placeholder,
            prefix = viewData.prefix,
            trailingButton = TrailingButton.Hidden,
        )

    /** A state of View in regard of user input. */
    sealed interface State<out Color> {

        /** Clears all user input. */
        data object Empty : State<Nothing>

        /** Populates UI with specified [color] data. */
        data class Populated<C>(val color: C) : State<C>

        /* No intermediate state with unfinished color */
    }

    /**
     * Applies specified [State] to [ColorInputFieldViewModel].
     */
    class StateReducer<Color>(private val colorToText: (Color) -> Text) {

        infix fun ColorInputFieldViewModel.apply(state: State<Color>) {
            when (state) {
                is State.Empty -> clearText()
                is State.Populated -> updateText(colorToText(state.color))
            }
        }
    }
}