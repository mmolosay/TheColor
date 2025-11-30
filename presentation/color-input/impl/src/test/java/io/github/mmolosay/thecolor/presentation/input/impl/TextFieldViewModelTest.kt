package io.github.mmolosay.thecolor.presentation.input.impl

import io.github.mmolosay.thecolor.domain.repository.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldViewModel
import io.github.mmolosay.thecolor.presentation.input.impl.field.updateText
import io.github.mmolosay.thecolor.presentation.input.impl.model.WithSource
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import io.github.mmolosay.thecolor.domain.model.UserPreferences.SelectAllTextOnTextFieldFocus as DomainSelectAllTextOnTextFieldFocus

internal class TextFieldViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    val testDispatcher = UnconfinedTestDispatcher()

    val coroutineScope = CoroutineScope(testDispatcher)

    val userPreferencesRepository: UserPreferencesRepository = mockk {
        every { flowOfSelectAllTextOnTextFieldFocus } returns kotlin.run {
            val value = DomainSelectAllTextOnTextFieldFocus(enabled = false)
            MutableStateFlow(value)
        }
    }

    lateinit var sut: TextFieldViewModel

    @Test
    fun `initial data has the initial text`() {
        createSut(
            initialText = "initial",
        )

        data.text shouldBe Text("initial")
    }

    @Test
    fun `initial data has the initial text even if 'select all text on text field focus' preference is 'null'`() {
        every { userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus } returns
                MutableStateFlow(null)

        createSut(
            initialText = "initial"
        )

        data.text shouldBe Text("initial")
    }

    @Test
    fun `text is updated when text is changed programmatically`() {
        createSut(
            initialText = "initial"
        )

        sut updateText Text("new")

        data.text shouldBe Text("new")
    }

    @Test
    fun `data update is not 'caused by user' when text is changed programmatically`() {
        createSut(
            initialText = "initial",
        )

        sut updateText Text("new")

        dataWithSource.causedByUser shouldBe false
    }

    @Test
    fun `text is updated when text is changed from View`() {
        createSut(
            initialText = "initial",
        )

        data.onTextChange(Text("new"))

        data.text shouldBe Text("new")
    }

    @Test
    fun `data update is 'caused by user' when text is changed from View`() {
        createSut(
            initialText = "initial",
        )

        data.onTextChange(Text("new"))

        dataWithSource.causedByUser shouldBe true
    }

    @Test // ANCHOR:Label=0
    fun `given that 'clear text' feature is enabled, when SUT is created with empty text, then 'clear text' feature is present`() {
        createSut(
            initialText = "",
            enableClearTextFeature = true,
        )

        data.clearText shouldNotBe null
    }

    @Test // ANCHOR:Label=1
    fun `given that 'clear text' feature is enabled, when SUT is created with non-empty text, then 'clear text' feature is present`() {
        createSut(
            initialText = "non-empty text",
            enableClearTextFeature = true,
        )

        data.clearText shouldNotBe null
    }

    @Test
    fun `given that 'clear text' feature is disabled, when SUT is created with empty text, then 'clear text' feature is absent`() {
        createSut(
            initialText = "",
            enableClearTextFeature = false,
        )

        data.clearText shouldBe null
    }

    @Test
    fun `given that 'clear text' feature is disabled, when SUT is created with non-empty text, then 'clear text' feature is absent`() {
        createSut(
            initialText = "non-empty text",
            enableClearTextFeature = false,
        )

        sut updateText Text("non-empty text")

        data.clearText shouldBe null
    }

    @Test
    fun `'clear text' feature is idempotent on initialization when text is empty`() {
        createSut(
            initialText = "",
            enableClearTextFeature = true,
        )

        // REFERENCE:Label=0
        data.clearText.shouldNotBeNull().willBeIdempotent shouldBe true
    }

    @Test
    fun `'clear text' feature is not idempotent on initialization when text is non-empty`() {
        createSut(
            initialText = "non-empty text",
            enableClearTextFeature = true,
        )

        // REFERENCE:Label=1
        data.clearText.shouldNotBeNull().willBeIdempotent shouldBe false
    }

    @Test
    fun `'clear text' feature is idempotent when text is changed from View and text is empty`() {
        createSut(
            initialText = "initial non-empty text",
            enableClearTextFeature = true,
        )

        data.onTextChange(Text(""))

        // REFERENCE:Label=0
        data.clearText.shouldNotBeNull().willBeIdempotent shouldBe true
    }

    @Test
    fun `'clear text' feature is not idempotent when text is changed from View and text is non-empty`() {
        createSut(
            initialText = "",
            enableClearTextFeature = true,
        )

        data.onTextChange(Text("non-empty text"))

        // REFERENCE:Label=1
        data.clearText.shouldNotBeNull().willBeIdempotent shouldBe false
    }

    @Test
    fun `text is cleared when 'clear text' feature is invoked`() {
        createSut(
            initialText = "initial non-empty text",
            enableClearTextFeature = true,
        )

        // REFERENCE:Label=1
        data.clearText.shouldNotBeNull().invoke()

        data.text shouldBe Text("")
    }

    @Test
    fun `data update is 'caused by user' when 'clear text' feature is invoked`() {
        createSut(
            initialText = "initial non-empty text",
            enableClearTextFeature = true,
        )

        // REFERENCE:Label=1
        data.clearText.shouldNotBeNull().invoke()

        dataWithSource.causedByUser shouldBe true
    }

    @Test
    fun `emissions of 'select all text on text field focus' preference are reflected in the data`() =
        runTest(testDispatcher) {
            val flowOfSelectAllTextOnTextFieldFocus =
                MutableStateFlow<DomainSelectAllTextOnTextFieldFocus?>(null) // initially empty
            every { userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus } returns
                    flowOfSelectAllTextOnTextFieldFocus
            createSut()
            sut updateText Text("initial")
            data.shouldSelectAllTextOnFocus shouldBe DefaultUserPreferences.SelectAllTextOnTextFieldFocus.enabled

            kotlin.run {
                val value = DomainSelectAllTextOnTextFieldFocus(enabled = false)
                flowOfSelectAllTextOnTextFieldFocus.emit(value)
            }
            data.shouldSelectAllTextOnFocus shouldBe false
            kotlin.run {
                val value = DomainSelectAllTextOnTextFieldFocus(enabled = true)
                flowOfSelectAllTextOnTextFieldFocus.emit(value)
            }
            data.shouldSelectAllTextOnFocus shouldBe true
        }

    fun createSut(
        initialText: String = "",
        enableClearTextFeature: Boolean = true,
    ) =
        TextFieldViewModel(
            initialText = initialText,
            coroutineScope = coroutineScope,
            filterUserInput = { Text(it) },
            enableClearTextFeature = enableClearTextFeature,
            userPreferencesRepository = userPreferencesRepository,
            defaultDispatcher = testDispatcher,
            uiDataUpdateDispatcher = testDispatcher,
        ).also {
            sut = it
        }

    val dataWithSource: WithSource<TextFieldData>
        get() = sut.dataFlow.value

    val data: TextFieldData
        get() = dataWithSource.data
}