package io.github.mmolosay.thecolor.presentation.devoptions.ui

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.ContentPadding
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.Description
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.Title
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import kotlinx.coroutines.launch

@Composable
internal fun BuildInfo(
    title: String,
    info: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    Surface(
        onClick = {
            val clipData = ClipData.newPlainText(/*label*/ title, /*text*/ info)
            val clipEntry = ClipEntry(clipData)
            coroutineScope.launch {
                clipboard.setClipEntry(clipEntry)
                Toast
                    .makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    ) {
        Column(
            modifier = modifier
                .padding(ContentPadding)
                .fillMaxWidth(),
        ) {
            Title(text = title)
            Description(text = info)
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun Preview() {
    TheColorTheme {
        BuildInfo(
            title = "Build info",
            info = "Build type: Debug\n" +
                    "App version name: 1.0.0\n" +
                    "App version code: 1\n" +
                    "Git head: 97cd4bd5",
        )
    }
}