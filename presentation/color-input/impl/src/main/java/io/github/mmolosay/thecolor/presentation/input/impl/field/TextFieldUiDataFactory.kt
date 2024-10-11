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
        trailingButton = TrailingButton(data.trailingButton, strings.trailingIcon),
    )

private fun TrailingButton(
    data: TextFieldData.TrailingButton,
    icon: TextFieldUiStrings.TrailingIcon,
): UiTrailingButton =
    when (data) {
        is DataTrailingButton.Hidden -> UiTrailingButton.Hidden
        is DataTrailingButton.Visible -> {
            when (icon) {
                is TextFieldUiStrings.TrailingIcon.None -> UiTrailingButton.Hidden // View didn't supply trailing icon, thus don't show trailing button
                is TextFieldUiStrings.TrailingIcon.Exists -> UiTrailingButton.Visible(
                    onClick = data.onClick,
                    iconContentDesc = icon.contentDesc
                )
            }
        }
    }