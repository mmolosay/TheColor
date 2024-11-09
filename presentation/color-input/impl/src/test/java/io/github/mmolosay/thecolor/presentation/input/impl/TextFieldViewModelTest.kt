package io.github.mmolosay.thecolor.presentation.input.impl

import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.TrailingButton
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldViewModel
import io.github.mmolosay.thecolor.presentation.input.impl.field.updateText
import io.github.mmolosay.thecolor.presentation.input.impl.model.Update
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.beOfType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import io.github.mmolosay.thecolor.domain.model.UserPreferences.SelectAllTextOnTextFieldFocus as DomainSelectAllTextOnTextFieldFocus

class TextFieldViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    val testDispatcher = UnconfinedTestDispatcher()

    val testScope = TestScope(testDispatcher)

    val userPreferencesRepository: UserPreferencesRepository = mockk {
        every { flowOfSelectAllTextOnTextFieldFocus() } returns kotlin.run {
            val value = DomainSelectAllTextOnTextFieldFocus(enabled = false)
            flowOf(value)
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
    fun `data is never initialized when text is changed but 'select all text on text field focus' preference wasn't obtained`() {
        val flowOfSelectAllTextOnTextFieldFocus =
            MutableSharedFlow<DomainSelectAllTextOnTextFieldFocus>() // initially empty
        every { userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus() } returns
                flowOfSelectAllTextOnTextFieldFocus
        createSut()

        sut updateText Text("initial")

        sut.dataUpdatesFlow.value shouldBe null
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

    @Test
    fun `trailing button is visible on initialization when text is non-empty`() {
        createSut()

        sut updateText Text("non-empty text")

        data.trailingButton should beOfType<TrailingButton.Visible>()
    }

    @Test
    fun `trailing button is hidden on initialization when text is empty`() {
        createSut()

        sut updateText Text("")

        data.trailingButton should beOfType<TrailingButton.Hidden>()
    }

    @Test
    fun `trailing button is visible when text is changed from UI and text is non-empty`() {
        createSut()
        sut updateText Text("initial")

        data.onTextChange(Text("non-empty text"))

        data.trailingButton should beOfType<TrailingButton.Visible>()
    }

    @Test
    fun `trailing button is hidden when text is changed from UI and text is empty`() {
        createSut()
        sut updateText Text("initial")

        data.onTextChange(Text(""))

        data.trailingButton should beOfType<TrailingButton.Hidden>()
    }

    @Test
    fun `text is cleared on trailing button click`() {
        createSut()
        sut updateText Text("initial non-empty text")

        (data.trailingButton as TrailingButton.Visible).onClick()

        data.text shouldBe Text("")
    }

    @Test
    fun `UiData update is caused by user when trailing button is clicked`() {
        createSut()
        sut updateText Text("initial non-empty text")

        (data.trailingButton as TrailingButton.Visible).onClick()

        dataUpdate.causedByUser shouldBe true
    }

    @Test
    fun `emissions of 'select all text on text field focus' preference are reflected in the data`() =
        runTest(testDispatcher) {
            val flowOfSelectAllTextOnTextFieldFocus =
                MutableSharedFlow<DomainSelectAllTextOnTextFieldFocus>() // initially empty
            every { userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus() } returns
                    flowOfSelectAllTextOnTextFieldFocus
            createSut()
            sut updateText Text("initial")

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

    fun createSut() =
        TextFieldViewModel(
            coroutineScope = testScope,
            filterUserInput = { Text(it) },
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