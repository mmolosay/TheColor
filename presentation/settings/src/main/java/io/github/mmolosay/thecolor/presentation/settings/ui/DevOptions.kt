package io.github.mmolosay.thecolor.presentation.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.settings.ui.ItemUiComponents.Title
import io.github.mmolosay.thecolor.presentation.settings.ui.UiComponents.DefaultItemContentPadding

@Composable
internal fun DevOptions(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
    ) {
        Box(
            modifier = modifier
                .padding(DefaultItemContentPadding)
                .fillMaxWidth(),
        ) {
            Title(text = title)
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun DevOptionsPreview() {
    TheColorTheme {
        DevOptions(
            title = "Developer options",
            onClick = {},
        )
    }
}