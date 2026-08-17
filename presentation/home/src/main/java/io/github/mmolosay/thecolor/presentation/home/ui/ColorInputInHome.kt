package io.github.mmolosay.thecolor.presentation.home.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.common.compose.Placeholder
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme

/**
 * The "bare" 'Color Input' [Composable], free of any 'Home'-specific logic.
 */
internal typealias BareColorInput =
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

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    TheColorTheme {
        Surface {
            ColorInputInHome { modifier ->
                Placeholder(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(120.dp),
                ) {
                    Text("Bare Color Input")
                }
            }
        }
    }
}