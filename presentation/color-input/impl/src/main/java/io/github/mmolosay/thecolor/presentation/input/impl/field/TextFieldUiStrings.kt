package io.github.mmolosay.thecolor.presentation.input.impl.field

/**
 * Strings that are pre-defined in UI and don't come from ViewModel.
 *
 * This object is created by View, since string resources are tied to platform-specific
 * components (like Context), which should be avoided in ViewModels.
 */
data class TextFieldUiStrings(
    val label: String,
    val placeholder: String,
    val prefix: String?,
    val trailingIconContentDesc: String?,
)