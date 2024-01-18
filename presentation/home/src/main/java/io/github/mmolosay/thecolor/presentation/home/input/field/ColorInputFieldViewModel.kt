package io.github.mmolosay.thecolor.presentation.home.input.field

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
    private val filterUserInput: (String) -> String,
) {

    private val _uiDataFlow = MutableStateFlow(makeInitialUiData())
    val uiDataFlow = _uiDataFlow.asStateFlow()

    // presentation-ready text is passed
    fun setText(text: String) {
        _uiDataFlow.update {
            it.smartCopy(text = text)
        }
    }

    fun clearText() {
        _uiDataFlow.update {
            it.smartCopy(text = "")
        }
    }

    // seems like a better solution than "uiDataFlow = _uiDataFlow.map {..}"
    private fun ColorInputFieldUiData.smartCopy(
        text: String,
    ) =
        copy(
            text = text,
            trailingButton = trailingButton(
                text = text,
                trailingIcon = viewData.trailingIcon,
            ),
        )

    private fun trailingButton(
        text: String,
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

    private fun showTrailingButton(text: String): Boolean =
        text.isNotEmpty()

    private fun makeInitialUiData() =
        ColorInputFieldUiData(
            text = "",
            onTextChange = ::setText,
            filterUserInput = filterUserInput,
            label = viewData.label,
            placeholder = viewData.placeholder,
            prefix = viewData.prefix,
            trailingButton = TrailingButton.Hidden,
        )
}