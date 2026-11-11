package io.github.mmolosay.thecolor.presentation.input

import android.os.Build
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SelectAllTextOnTextFieldFocus
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.PrefState
import io.github.mmolosay.thecolor.presentation.input.textfield.TextField
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldCore
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldFeature
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldTestTags
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldUiStrings
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFormatter
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.annotation.Config
import tech.apter.junit.jupiter.robolectric.RobolectricExtension

@OptIn(ExperimentalTestApi::class)
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class TextFieldTest {

    @Test
    fun `given the field shows '1', when the user clears it and types '5' before the 'viewData' collector runs, then the field shows '5'`() =
        // queues the collector after a write, as the main thread does on a device; by default, the
        // test would run it within the write
        runComposeUiTest(effectContext = StandardTestDispatcher()) {
            createSut(text = "1")
            val clearButton = onNodeWithTag(TextFieldTestTags.ClearTextButton)
            val textField = onNode(hasSetTextAction())

            // in one main-thread task, so that the collector can't run in between: as on a device,
            // when the key is already queued while the click is handled
            runOnUiThread {
                clearButton.performClick()
                textField.performTextInput("5")
            }

            textField.editableText shouldBe "5"
        }

    fun ComposeUiTest.createSut(text: String) {
        val core = TextFieldCore(
            formatter = TextFormatter { text -> text },
        )
        val userPreferencesRepository: UserPreferencesRepository = mockk {
            every { flowOfSelectAllTextOnTextFieldFocus } returns run {
                val value = PrefState.BeingInitialized
                MutableStateFlow<PrefState<SelectAllTextOnTextFieldFocus>>(value)
            }
        }
        val feature = TextFieldFeature(
            core = core,
            store = Store(initialState = core.initialState(text)),
            scope = CoroutineScope(Job() + Dispatchers.Unconfined),
            userPreferencesRepository = userPreferencesRepository,
        )
        val strings = TextFieldUiStrings(
            label = "HEX",
            placeholder = "000000",
            prefix = null,
            trailingIconContentDesc = "Clear text",
        )
        setContent {
            TextField(
                feature = feature,
                strings = strings,
                hasClearTextButton = true,
                keyboardOptions = KeyboardOptions.Default,
                onKeyboardAction = null,
            )
        }
    }

    val SemanticsNodeInteraction.editableText: String
        get() = fetchSemanticsNode().config[SemanticsProperties.EditableText].text
}
