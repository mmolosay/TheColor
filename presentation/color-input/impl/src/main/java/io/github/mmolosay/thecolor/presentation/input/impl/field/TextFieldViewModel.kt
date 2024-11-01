package io.github.mmolosay.thecolor.presentation.input.impl.field

import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.TrailingButton
import io.github.mmolosay.thecolor.presentation.input.impl.model.Update
import io.github.mmolosay.thecolor.presentation.input.impl.model.causedByUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Handles presentation logic of a single text field inside a 'Color Input' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
class TextFieldViewModel(
    private val filterUserInput: (String) -> Text,
) {

    private val _dataUpdatesFlow = MutableStateFlow<Update<TextFieldData>?>(null)
    val dataUpdatesFlow = _dataUpdatesFlow.asStateFlow()

    private fun updateText(update: Update<Text>) {
        _dataUpdatesFlow.update {
            val text = update.payload
            val newData = if (it == null) {
                makeInitialData(text)
            } else {
                val oldData = it.payload
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