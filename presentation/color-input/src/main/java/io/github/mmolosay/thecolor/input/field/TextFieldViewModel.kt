package io.github.mmolosay.thecolor.input.field

import io.github.mmolosay.thecolor.input.Update
import io.github.mmolosay.thecolor.input.causedByUser
import io.github.mmolosay.thecolor.input.field.TextFieldUiData.Text
import io.github.mmolosay.thecolor.input.field.TextFieldUiData.TrailingButton
import io.github.mmolosay.thecolor.input.field.TextFieldUiData.ViewData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Not a ViewModel-ViewModel in terms of Android development.
 * It doesn't derive from [androidx.lifecycle.ViewModel], so should only be used in "real" ViewModels
 * which do derive from Android-aware implementation.
 */
class TextFieldViewModel(
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
            val oldUiData = it.data
            val newUiData = oldUiData.smartCopy(text)
            newUiData causedByUser update.causedByUser
        }
    }

    private fun onTextChangeFromView(text: Text) =
        updateText(text causedByUser true)

    private fun TextFieldUiData.smartCopy(text: Text) =
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
        TextFieldUiData(
            text = text,
            onTextChange = ::onTextChangeFromView,
            filterUserInput = filterUserInput,
            label = viewData.label,
            placeholder = viewData.placeholder,
            prefix = viewData.prefix,
            trailingButton = trailingButton(text, viewData.trailingIcon),
        )

    companion object {

        /*
         * Curious thing to notice:
         * Used method of ViewModel is private,
         * however you still will be able to invoke it indirectly using this companion object.
         * I find this approach to be a great alternative to exposing ViewModel methods as public.
         */
        infix fun TextFieldViewModel.updateWith(text: Text) {
            updateText(text causedByUser false) // not a data from UI
        }
    }
}