package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * The "bare" 'Color Input' [Composable], free of any 'Home'-specific logic.
 */
typealias BareColorInput =
        @Composable (Modifier) -> Unit

/**
 * 'Color Input' as the 'Home' feature presents it.
 */
@Composable
internal fun ColorInputInHome(
    colorInput: BareColorInput,
) {
    val modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
    colorInput(modifier)
}