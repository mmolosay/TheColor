package io.github.mmolosay.thecolor.presentation.input.impl.field

import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.TrailingButton as DataTrailingButton
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldUiData.TrailingButton as UiTrailingButton

fun TextFieldUiData(
    data: TextFieldData,
    strings: TextFieldUiStrings,
): TextFieldUiData =
    TextFieldUiData(
        text = data.text,
        onTextChange = data.onTextChange,
        filterUserInput = data.filterUserInput,
        label = strings.label,
        placeholder = strings.placeholder,
        prefix = strings.prefix,
        trailingButton = TrailingButton(
            data = data.trailingButton,
            iconContentDesc = strings.trailingIconContentDesc,
        ),
    )

private fun TrailingButton(
    data: TextFieldData.TrailingButton,
    iconContentDesc: String?,
): UiTrailingButton =
    when (data) {
        is DataTrailingButton.Hidden -> {
            UiTrailingButton.Hidden
        }
        is DataTrailingButton.Visible -> {
            if (iconContentDesc != null) {
                UiTrailingButton.Visible(
                    onClick = data.onClick,
                    iconContentDesc = iconContentDesc,
                )
            } else {
                UiTrailingButton.Hidden
            }
        }
    }