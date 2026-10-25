package io.github.mmolosay.thecolor.presentation.home.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.common.compose.Placeholder
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorHandle
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroup
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupHandle
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHex
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsv
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgb

/**
 * The "bare" 'Color Input' [Composable], free of any 'Home'-specific logic.
 */
internal typealias BareColorInputComposable =
        @Composable (Modifier) -> Unit

@Composable
internal fun BareColorInput(
    editorHandle: ColorEditorHandle,
    groupHandle: ColorInputGroupHandle,
    modifier: Modifier = Modifier,
) {
    val editorFacade = run {
        val handle = editorHandle
        val data = editorHandle.dataFlow.collectAsStateWithLifecycle().value
        remember(handle, data) { handle.facade(data) }
    }
    ColorInputGroup(
        modifier = modifier,
        handle = groupHandle,
        hexInput = {
            ColorInputHex(facade = editorFacade.hex)
        },
        rgbInput = {
            ColorInputRgb(facade = editorFacade.rgb)
        },
        hsvInput = {
            ColorInputHsv(facade = editorFacade.hsv)
        },
    )
}

/**
 * 'Color Input' as the 'Home' feature presents it.
 */
@Composable
internal fun ColorInputInHome(
    content: BareColorInputComposable,
) {
    val modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
    content(modifier)
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