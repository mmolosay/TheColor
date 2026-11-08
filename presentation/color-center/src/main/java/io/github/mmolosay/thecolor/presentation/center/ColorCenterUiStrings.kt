package io.github.mmolosay.thecolor.presentation.center

/**
 * Strings that are pre-defined in UI and don't come from ViewModel.
 *
 * This object is created by View, since string resources are tied to platform-specific
 * components (like Context), which should be avoided in ViewModels.
 */
data class ColorCenterUiStrings(
    val page1ChangePageButtonText: String,
    val page2ChangePageButtonText: String,
)