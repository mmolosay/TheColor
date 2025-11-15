package io.github.mmolosay.thecolor.presentation.acknowledgements

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.util.htmlReadyLicenseContent
import io.github.mmolosay.debounce.debounced
import kotlin.time.Duration.Companion.milliseconds
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcknowledgementsScreen(
    navigateBack: () -> Unit,
) {
    val libraries by produceLibraries()
    val strings = AcknowledgementsUiStrings(LocalContext.current)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                strings = strings,
                scrollBehavior = scrollBehavior,
                navigateBack = navigateBack,
            )
        },
    ) { padding ->
        val showAuthor = true
        var licensesDialogState by remember {
            mutableStateOf<LicensesDialogState>(LicensesDialogState.Hidden)
        }
        LibrariesContainer(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            libraries = libraries,
            contentPadding = padding,
            showAuthor = showAuthor,
            textStyles = LibraryDefaults.libraryTextStyles(
                nameTextStyle = MaterialTheme.typography.titleMedium,
                authorTextStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                licensesTextStyle = MaterialTheme.typography.bodyMedium,
                fundingTextStyle = MaterialTheme.typography.bodyMedium,
            ),
            onLibraryClick = { library ->
                licensesDialogState = LicensesDialogState.Visible(library)
            },
            author = { author ->
                if (showAuthor && author.isNotBlank()) {
                    Author(text = author)
                }
            },
        )

        licensesDialogState.let { dialogState ->
            if (dialogState !is LicensesDialogState.Visible) return@let
            LicensesDialog(
                library = dialogState.library,
                onDismissRequest = {
                    @Suppress("AssignedValueIsNeverRead") // false warning
                    licensesDialogState = LicensesDialogState.Hidden
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    strings: AcknowledgementsUiStrings,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateBack: () -> Unit,
) {
    val debouncedNavigateBack = remember(navigateBack) {
        debounced(
            action = navigateBack,
            timeout = 1000.milliseconds,
        )
    }
    LargeTopAppBar(
        title = {
            Text(text = strings.topBarTitle)
        },
        navigationIcon = {
            IconButton(
                onClick = debouncedNavigateBack,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(DesignR.drawable.ic_arrow_back),
                    contentDescription = strings.topBarGoBackIconDesc,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(),
        scrollBehavior = scrollBehavior,
    )
}

private sealed interface LicensesDialogState {
    data object Hidden : LicensesDialogState
    data class Visible(val library: Library) : LicensesDialogState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LicensesDialog(
    library: Library,
    onDismissRequest: () -> Unit,
) {
    val text = remember(library) {
        library.htmlReadyLicenseContent
            .takeIf { it.isNotEmpty() }
            ?.let { AnnotatedString.fromHtml(it) }
    } ?: return
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = text,
        )
    }
}

@Composable
private fun Author(
    text: String,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}