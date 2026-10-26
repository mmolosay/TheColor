package io.github.mmolosay.thecolor.demo

import android.app.Application
import android.os.Build
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextField
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldCore
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFormatter
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import tech.apter.junit.jupiter.robolectric.RobolectricExtension

/**
 * Tests the [TextField] View with its feature, on a store of its own.
 *
 * The frame clock is paused, so that the user can act between two frames: as on a device, when a key
 * arrives before the screen redraws.
 */
@OptIn(ExperimentalTestApi::class)
@ExtendWith(RobolectricExtension::class)
@Config(
    sdk = [Build.VERSION_CODES.TIRAMISU],
    application = Application::class, // the demo's own one sets up Hilt, which the test doesn't need
)
class TextFieldTest {

    // keeps any text, so that only the expected value decides whether an edit applies
    val core = TextFieldCore(formatter = TextFormatter { text -> text })

    lateinit var field: TextFieldFeature

    @Test
    fun `given the field shows '1', when the user clears it and types '5' before the next frame, then the field shows '5'`() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            createSut(text = "1")

            onNodeWithContentDescription(clearLabel).performSemanticsAction(SemanticsActions.OnClick)
            onNode(hasSetTextAction()).performTextInput("5")
            mainClock.advanceTimeByFrame()

            shownText shouldBe "5"
        }

    fun ComposeUiTest.createSut(text: String) {
        field = TextFieldFeature(
            scope = CoroutineScope(Job()),
            store = Store(initialState = core.initialState(text)),
            core = core,
        )
        setContent {
            TextField(
                feature = field,
                label = "Grams",
                keyboardOptions = KeyboardOptions.Default,
                onKeyboardAction = null,
                hasClearButton = true,
                supportingText = null,
                outputTransformation = null,
            )
        }
        mainClock.advanceTimeByFrame() // the field shows its text, and the clear button shows
    }

    val clearLabel: String
        get() = RuntimeEnvironment.getApplication().getString(R.string.text_field_clear)

    // the text of the field itself, without its label
    val ComposeUiTest.shownText: String
        get() {
            val core = onNode(hasSetTextAction()).fetchSemanticsNode()
            return core.config[SemanticsProperties.EditableText].text
        }
}
