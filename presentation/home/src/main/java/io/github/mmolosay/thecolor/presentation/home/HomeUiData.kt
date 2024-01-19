package io.github.mmolosay.thecolor.presentation.home

data class HomeUiData(
    val title: String,
    val proceedButton: Button,
) {

    data class Button(
        val onClick: () -> Unit,
        val enabled: Boolean,
        val text: String,
    )

    data class ViewData(
        val title: String,
        val proceedButtonText: String,
    )
}