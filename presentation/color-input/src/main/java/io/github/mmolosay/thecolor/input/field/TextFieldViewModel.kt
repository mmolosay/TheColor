package io.github.mmolosay.thecolor.input.field

import io.github.mmolosay.thecolor.input.field.TextFieldData.Text
import io.github.mmolosay.thecolor.input.field.TextFieldData.TrailingButton
import io.github.mmolosay.thecolor.input.model.Update
import io.github.mmolosay.thecolor.input.model.causedByUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Not a ViewModel-ViewModel in terms of Android development.
 * It doesn't derive from [androidx.lifecycle.ViewModel], so should only be used in "real" ViewModels
 * which do derive from Android-aware implementation.
 */
class TextFieldViewModel(
    private val filterUserInput: (String) -> Text,
) {

    private val _dataUpdatesFlow = MutableStateFlow<Update<TextFieldData>?>(null)
    val dataUpdatesFlow = _dataUpdatesFlow.asStateFlow()

    private fun updateText(update: Update<Text>) {
        _dataUpdatesFlow.update {
            val text = update.data
            val newData = if (it == null) {
                makeInitialData(text)
            } else {
                val oldData = it.data
                oldData.smartCopy(text)
            }
            newData causedByUser update.causedByUser
        }
    }

    private fun onTextChangeFromView(text: Text) =
        updateText(text causedByUser true)

    private fun TextFieldData.smartCopy(text: Text) =
        copy(
            text = text,
            trailingButton = trailingButton(text),
        )

    private fun trailingButton(text: Text): TrailingButton =
        when (showTrailingButton(text)) {
            true -> TrailingButton.Visible(onClick = { onTextChangeFromView(Text("")) })
            false -> TrailingButton.Hidden
        }

    private fun showTrailingButton(text: Text): Boolean =
        text.string.isNotEmpty()

    private fun makeInitialData(text: Text) =
        TextFieldData(
            text = text,
            onTextChange = ::onTextChangeFromView,
            filterUserInput = filterUserInput,
            trailingButton = trailingButton(text),
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