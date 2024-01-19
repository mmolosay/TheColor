package io.github.mmolosay.thecolor.presentation.home.input.field

import io.github.mmolosay.thecolor.presentation.home.input.Update
import io.github.mmolosay.thecolor.presentation.home.input.causedByUser
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
    initialText: Text,
    private val viewData: ViewData,
    private val filterUserInput: (String) -> Text,
) {

    private val _uiDataUpdatesFlow = MutableStateFlow(
        makeInitialUiData(text = initialText) causedByUser false
    )
    val uiDataUpdatesFlow = _uiDataUpdatesFlow.asStateFlow()

    private fun updateText(update: Update<Text>) {
        _uiDataUpdatesFlow.update {
            val text = update.data
            val (oldUiData) = it
            val newUiData = oldUiData.smartCopy(text)
            newUiData causedByUser update.causedByUser
        }
    }

    // seems like a better solution than "uiDataFlow = _uiDataFlow.map {..}"
    private fun ColorInputFieldUiData.smartCopy(text: Text) =
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
                onClick = { updateText(Text("") causedByUser true) },
                iconContentDesc = trailingIcon.contentDesc,
            )
        } else {
            TrailingButton.Hidden
        }

    private fun showTrailingButton(text: Text): Boolean =
        text.string.isNotEmpty()

    private fun makeInitialUiData(text: Text) =
        ColorInputFieldUiData(
            text = text,
            onTextChange = { new -> updateText(new causedByUser true) },
            filterUserInput = filterUserInput,
            label = viewData.label,
            placeholder = viewData.placeholder,
            prefix = viewData.prefix,
            trailingButton = trailingButton(text, viewData.trailingIcon),
        )

    /** A state of View in regard of user input. */
    sealed interface State<out ColorInput> {

        /** Clears all user input. */
        data object Empty : State<Nothing>

        /** Populates UI with specified [color] data. */
        data class Populated<C>(val color: C) : State<C>

        /* No intermediate state with unfinished color */
    }

    /**
     * Applies specified [State] to [ColorInputFieldViewModel].
     */
    // TODO: ColorInputPopulator? and get rid of State? because State.Empty is a State.Populated with empty fields
    class StateReducer<Color>(private val colorToText: (Color) -> Text) {

        /*
         * Curious thing to notice:
         * both used methods of ViewModel are private.
         * however if this class is instantiated outside of ViewModel,
         * you will be able to invoke them indirectly using this method.
         * I find this approach to be a great alternative to exposing ViewModel methods as public.
         */
        infix fun ColorInputFieldViewModel.apply(state: State<Color>) {
            val text = when (state) {
                is State.Empty -> Text("")
                is State.Populated -> colorToText(state.color)
            }
            updateText(text causedByUser false)
        }
    }
}