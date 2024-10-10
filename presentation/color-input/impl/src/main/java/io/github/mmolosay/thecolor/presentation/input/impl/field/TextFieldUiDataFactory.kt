package io.github.mmolosay.thecolor.presentation.input.impl.field

import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldUiData.ViewData
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.TrailingButton as DataTrailingButton
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldUiData.TrailingButton as UiTrailingButton

operator fun TextFieldData.plus(viewData: ViewData): TextFieldUiData =
    TextFieldUiData(data = this, viewData = viewData)

private fun TextFieldUiData(
    data: TextFieldData,
    viewData: ViewData,
): TextFieldUiData =
    TextFieldUiData(
        text = data.text,
        onTextChange = data.onTextChange,
        filterUserInput = data.filterUserInput,
        label = viewData.label,
        placeholder = viewData.placeholder,
        prefix = viewData.prefix,
        trailingButton = TrailingButton(data.trailingButton, viewData.trailingIcon),
    )

private fun TrailingButton(
    data: TextFieldData.TrailingButton,
    icon: ViewData.TrailingIcon,
): UiTrailingButton =
    when (data) {
        is DataTrailingButton.Hidden -> UiTrailingButton.Hidden
        is DataTrailingButton.Visible -> {
            when (icon) {
                is ViewData.TrailingIcon.None -> UiTrailingButton.Hidden // View didn't supply trailing icon, thus don't show trailing button
                is ViewData.TrailingIcon.Exists -> UiTrailingButton.Visible(
                    onClick = data.onClick,
                    iconContentDesc = icon.contentDesc
                )
            }
        }
    }