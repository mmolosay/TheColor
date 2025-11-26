package io.github.mmolosay.thecolor.presentation.input.impl

import io.github.mmolosay.thecolor.domain.repository.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldViewModel
import io.github.mmolosay.thecolor.presentation.input.impl.field.updateText
import io.github.mmolosay.thecolor.presentation.input.impl.model.Update
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
    fun `SUT is created with 'null' data`() {
        createSut()

        sut.dataUpdatesFlow.value shouldBe null
    }

    @Test
    fun `data is initialized when text is changed`() {
        createSut()

        sut updateText Text("initial")

        sut.dataUpdatesFlow.value shouldNotBe null
    }

    @Test
    fun `data is initialized when text is changed but 'select all text on text field focus' preference is 'null'`() {
        every { userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus } returns
                MutableStateFlow(null)
        createSut()

        sut updateText Text("initial")

        sut.dataUpdatesFlow.value shouldNotBe null
    }

    @Test
    fun `text is updated when test is changed`() {
        createSut()
        sut updateText Text("initial")

        sut updateText Text("new")

        data.text shouldBe Text("new")
    }

    @Test
    fun `data update is not caused by user when text is changed`() {
        createSut()

        sut updateText Text("initial")

        dataUpdate.causedByUser shouldBe false
    }

    @Test
    fun `text is updated when text is changed from UI`() {
        createSut()
        sut updateText Text("initial")

        data.onTextChange(Text("new"))

        data.text shouldBe Text("new")
    }

    @Test
    fun `data update is caused by user when text is changed from UI`() {
        createSut()
        sut updateText Text("initial")

        data.onTextChange(Text("new"))

        dataUpdate.causedByUser shouldBe true
    }

    @Test // ANCHOR:Label=0
    fun `given that 'clear text' feature is enabled, when SUT is created with empty text, then 'clear text' feature is present`() {
        createSut(
            enableClearTextFeature = true
        )

        sut updateText Text("")

        data.clearText shouldNotBe null
    }

    @Test // ANCHOR:Label=1
    fun `given that 'clear text' feature is enabled, when SUT is created with non-empty text, then 'clear text' feature is present`() {
        createSut(
            enableClearTextFeature = true
        )

        sut updateText Text("non-empty text")

        data.clearText shouldNotBe null
    }

    @Test
    fun `given that 'clear text' feature is disabled, when SUT is created with empty text, then 'clear text' feature is absent`() {
        createSut(
            enableClearTextFeature = false
        )

        sut updateText Text("")

        data.clearText shouldBe null
    }

    @Test
    fun `given that 'clear text' feature is disabled, when SUT is created with non-empty text, then 'clear text' feature is absent`() {
        createSut(
            enableClearTextFeature = false
        )

        sut updateText Text("non-empty text")

        data.clearText shouldBe null
    }

    @Test
    fun `'clear text' feature is idempotent on initialization when text is empty`() {
        createSut(
            enableClearTextFeature = true
        )

        sut updateText Text("")

        // REFERENCE:Label=0
        data.clearText.shouldNotBeNull().willBeIdempotent() shouldBe true
    }

    @Test
    fun `'clear text' feature is not idempotent on initialization when text is non-empty`() {
        createSut(
            enableClearTextFeature = true
        )

        sut updateText Text("non-empty text")

        // REFERENCE:Label=1
        data.clearText.shouldNotBeNull().willBeIdempotent() shouldBe false
    }

    @Test
    fun `'clear text' feature is idempotent when text is changed from UI and text is empty`() {
        createSut(
            enableClearTextFeature = true
        )
        sut updateText Text("initial")

        data.onTextChange(Text(""))

        // REFERENCE:Label=0
        data.clearText.shouldNotBeNull().willBeIdempotent() shouldBe true
    }

    @Test
    fun `'clear text' feature is not idempotent when text is changed from UI and text is non-empty`() {
        createSut(
            enableClearTextFeature = true
        )
        sut updateText Text("initial")

        data.onTextChange(Text("non-empty text"))

        // REFERENCE:Label=1
        data.clearText.shouldNotBeNull().willBeIdempotent() shouldBe false
    }

    @Test
    fun `text is cleared when 'clear text' feature is invoked`() {
        createSut(
            enableClearTextFeature = true
        )
        sut updateText Text("initial non-empty text")

        // REFERENCE:Label=1
        data.clearText.shouldNotBeNull().invoke()

        data.text shouldBe Text("")
    }

    @Test
    fun `data update is caused by user when 'clear text' feature is invoked`() {
        createSut(
            enableClearTextFeature = true
        )
        sut updateText Text("initial non-empty text")

        // REFERENCE:Label=1
        data.clearText.shouldNotBeNull().invoke()

        dataUpdate.causedByUser shouldBe true
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
        enableClearTextFeature: Boolean = true,
    ) =
        TextFieldViewModel(
            coroutineScope = coroutineScope,
            filterUserInput = { Text(it) },
            enableClearTextFeature = enableClearTextFeature,
            userPreferencesRepository = userPreferencesRepository,
            defaultDispatcher = testDispatcher,
            uiDataUpdateDispatcher = testDispatcher,
        ).also {
            sut = it
        }

    val dataUpdate: Update<TextFieldData>
        get() = requireNotNull(sut.dataUpdatesFlow.value)

    val data: TextFieldData
        get() = dataUpdate.payload
}