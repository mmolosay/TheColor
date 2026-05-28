package io.github.mmolosay.thecolor.presentation.acknowledgements

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.State
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Developer
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.entity.License
import com.mikepenz.aboutlibraries.entity.Scm
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.util.htmlReadyLicenseContent
import io.github.mmolosay.debounce.debounced
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import kotlin.time.Duration.Companion.milliseconds
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries as produceLibrariesByDefault
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcknowledgementsScreen(
    navigateBack: () -> Unit,
    produceLibraries: @Composable () -> State<Libs?> = { produceLibrariesByDefault() },
) {
    val context = LocalContext.current
    val libraries by produceLibraries()
    val strings = AcknowledgementsUiStrings(LocalContext.current)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var licensesDialogState by remember {
        mutableStateOf<LicensesDialogState>(LicensesDialogState.Hidden)
    }

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
        Acknowledgements(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            libraries = libraries,
            contentPadding = padding,
            onLibraryClick = { library ->
                val text = library.htmlReadyLicenseContent
                    .takeIf { it.isNotBlank() }
                    ?.let { AnnotatedString.fromHtml(it) }
                if (text != null) {
                    licensesDialogState = LicensesDialogState.Visible(text)
                } else {
                    Toast
                        .makeText(context, strings.noLicensesFoundToastMessage, Toast.LENGTH_SHORT)
                        .show()
                }
            },
        )
    }

    licensesDialogState.let { dialogState ->
        if (dialogState !is LicensesDialogState.Visible) return@let
        LicensesDialog(
            text = dialogState.text,
            onDismissRequest = {
                @Suppress("AssignedValueIsNeverRead") // false warning
                licensesDialogState = LicensesDialogState.Hidden
            },
        )
    }
}

@Composable
private fun Acknowledgements(
    modifier: Modifier,
    libraries: Libs?,
    contentPadding: PaddingValues,
    onLibraryClick: (Library) -> Unit,
) {
    val showAuthor = true
    LibrariesContainer(
        modifier = modifier,
        libraries = libraries,
        contentPadding = contentPadding,
        showAuthor = showAuthor,
        textStyles = LibraryDefaults.libraryTextStyles(
            nameTextStyle = MaterialTheme.typography.titleMedium,
            authorTextStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            licensesTextStyle = MaterialTheme.typography.bodyMedium,
            fundingTextStyle = MaterialTheme.typography.bodyMedium,
        ),
        onLibraryClick = onLibraryClick,
        author = { author ->
            if (showAuthor && author.isNotBlank()) {
                Author(text = author)
            }
        },
    )
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
    data class Visible(val text: AnnotatedString) : LicensesDialogState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LicensesDialog(
    text: AnnotatedString,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            modifier = Modifier
                .verticalScroll(state = rememberScrollState())
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

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    TheColorTheme {
        AcknowledgementsScreen(
            navigateBack = {},
            produceLibraries = { remember { mutableStateOf(previewLibraries()) } },
        )
    }
}

@Suppress("SpellCheckingInspection")
private fun previewLibraries(): Libs {
    val apacheLicense = License(
        name = "Apache License 2.0",
        url = "https://spdx.org/licenses/Apache-2.0.html",
        year = "",
        spdxId = "Apache-2.0",
        licenseContent = "<License content>",
        hash = "Apache-2.0",
    )
    return Libs(
        libraries = listOf(
            Library(
                uniqueId = "io.github.mmolosay:debounce",
                artifactVersion = "1.2.0",
                name = "debounce",
                description = "Debounce your lambdas.",
                website = "https://github.com/mmolosay/debounce",
                developers = listOf(
                    Developer(name = "Misha Malasai", organisationUrl = ""),
                ),
                organization = null,
                scm = Scm(
                    connection = "scm:git:git://github.com/mmolosay/debounce.git",
                    developerConnection = "scm:git:ssh://github.com/mmolosay/debounce.git",
                    url = "https://github.com/mmolosay/debounce",
                ),
                licenses = setOf(apacheLicense),
                funding = setOf(),
            ),
            Library(
                uniqueId = "org.jetbrains.kotlin:kotlin-stdlib",
                artifactVersion = "2.2.20",
                name = "Kotlin Stdlib",
                description = "Kotlin Standard Library",
                website = "https://kotlinlang.org/",
                developers = listOf(
                    Developer(name = "Kotlin Team", organisationUrl = "https://www.jetbrains.com"),
                ),
                organization = null,
                scm = Scm(
                    connection = "scm:git:https://github.com/JetBrains/kotlin.git",
                    developerConnection = "scm:git:https://github.com/JetBrains/kotlin.git",
                    url = "https://github.com/JetBrains/kotlin",
                ),
                licenses = setOf(apacheLicense),
                funding = setOf(),
            ),
        ),
        licenses = setOf(apacheLicense),
    )
}