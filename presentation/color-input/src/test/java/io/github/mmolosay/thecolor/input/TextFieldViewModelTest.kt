package io.github.mmolosay.thecolor.input

import io.github.mmolosay.thecolor.input.field.TextFieldUiData
import io.github.mmolosay.thecolor.input.field.TextFieldUiData.Text
import io.github.mmolosay.thecolor.input.field.TextFieldUiData.TrailingButton
import io.github.mmolosay.thecolor.input.field.TextFieldUiData.ViewData.TrailingIcon
import io.github.mmolosay.thecolor.input.field.TextFieldViewModel
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.beOfType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TextFieldViewModelTest {

    val viewData: TextFieldUiData.ViewData = mockk(relaxed = true) {
        every { trailingIcon } returns TrailingIcon.None
    }

    lateinit var sut: TextFieldViewModel

    val uiDataUpdate: Update<TextFieldUiData>
        get() = sut.uiDataUpdatesFlow.value

    val uiData: TextFieldUiData
        get() = uiDataUpdate.data

    @Test
    fun `initial text is set to initial UiData`() = runTest {
        val initialText = Text("anything")

        createSut(initialText)

        uiData.text shouldBe initialText
    }

    @Test
    fun `initial UiData update is not caused by user`() = runTest {
        createSut()

        uiDataUpdate.causedByUser shouldBe false
    }

    @Test
    fun `text change causes update of UiData by user`() = runTest {
        createSut()
        val initialUiData = uiData

        uiData.onTextChange(Text("new"))
        val newUiData = uiData

        newUiData shouldNotBe initialUiData
        uiDataUpdate.causedByUser shouldBe true
    }

    @Test
    fun `trailing button is visible when text is non-empty`() = runTest {
        every { viewData.trailingIcon } returns mockk<TrailingIcon.Exists>(relaxed = true)
        createSut()

        uiData.onTextChange(Text("non-empty text"))

        uiData.trailingButton should beOfType<TrailingButton.Visible>()
    }

    @Test
    fun `trailing button is hidden when text is empty`() = runTest {
        every { viewData.trailingIcon } returns mockk<TrailingIcon.Exists>(relaxed = true)
        createSut()

        uiData.onTextChange(Text(""))

        uiData.trailingButton should beOfType<TrailingButton.Hidden>()
    }

    @Test
    fun `trailing button is hidden when there should be no such`() = runTest {
        every { viewData.trailingIcon } returns TrailingIcon.None
        createSut()

        uiData.onTextChange(Text("any text"))

        uiData.trailingButton should beOfType<TrailingButton.Hidden>()
    }

    @Test
    fun `text is cleared on trailing button click`() = runTest {
        every { viewData.trailingIcon } returns mockk<TrailingIcon.Exists>(relaxed = true)
        createSut(initialText = Text("non empty so trailing button is visible"))

        (uiData.trailingButton as TrailingButton.Visible).onClick()

        uiData.text.string shouldBe ""
    }

    @Test
    fun `trailing button click causes update of UiData by user`() = runTest {
        every { viewData.trailingIcon } returns mockk<TrailingIcon.Exists>(relaxed = true)
        createSut(initialText = Text("non empty so trailing button is visible"))

        (uiData.trailingButton as TrailingButton.Visible).onClick()

        uiDataUpdate.causedByUser shouldBe true
    }

    fun createSut(
        initialText: Text = Text(""),
        filterUserInput: (String) -> Text = noopFilterUserInput,
    ) =
        TextFieldViewModel(
            initialText = initialText,
            viewData = viewData,
            filterUserInput = filterUserInput,
        ).also { this.sut = it }

    val noopFilterUserInput: (String) -> Text = { Text(it) }
}