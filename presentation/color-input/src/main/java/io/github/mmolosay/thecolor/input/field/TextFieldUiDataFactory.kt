package io.github.mmolosay.thecolor.input.field

import io.github.mmolosay.thecolor.input.field.TextFieldUiData.ViewData
import io.github.mmolosay.thecolor.input.field.TextFieldData.TrailingButton as DataTrailingButton
import io.github.mmolosay.thecolor.input.field.TextFieldUiData.TrailingButton as UiTrailingButton

operator fun TextFieldData.plus(viewData: ViewData): TextFieldUiData =
    combine(data = this, viewData = viewData)

private fun combine(
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
        trailingButton = data.trailingButton.toUiTrailingButton(viewData.trailingIcon),
    )

private fun DataTrailingButton.toUiTrailingButton(
    trailingIcon: ViewData.TrailingIcon,
): UiTrailingButton =
    when (this) {
        is DataTrailingButton.Hidden -> UiTrailingButton.Hidden
        is DataTrailingButton.Visible -> {
            when (trailingIcon) {
                is ViewData.TrailingIcon.None -> UiTrailingButton.Hidden
                is ViewData.TrailingIcon.Exists -> UiTrailingButton.Visible(
                    onClick = this.onClick,
                    iconContentDesc = trailingIcon.contentDesc
                )
            }
        }
    }