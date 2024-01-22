package io.github.mmolosay.thecolor.input

import io.github.mmolosay.thecolor.input.field.TextFieldUiData
import io.github.mmolosay.thecolor.input.field.TextFieldUiData.Text
import io.github.mmolosay.thecolor.input.field.TextFieldUiData.TrailingButton
import io.github.mmolosay.thecolor.input.field.TextFieldUiData.ViewData.TrailingIcon
import io.github.mmolosay.thecolor.input.field.TextFieldViewModel
import io.github.mmolosay.thecolor.input.field.TextFieldViewModel.Companion.updateWith
import io.github.mmolosay.thecolor.input.model.Update
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class TextFieldViewModelTest {

    val viewData: TextFieldUiData.ViewData = mockk(relaxed = true) {
        every { trailingIcon } returns TrailingIcon.None
    }

    lateinit var sut: TextFieldViewModel

    val uiDataUpdate: Update<TextFieldUiData>
        get() = requireNotNull(sut.uiDataUpdatesFlow.value)

    val uiData: TextFieldUiData
        get() = uiDataUpdate.data

    @Test
    fun `sut is created with null UiData`() {
        createSut()

        sut.uiDataUpdatesFlow.value shouldBe null
    }

    @Test
    fun `UiData is initialized when text is changed by companion`() {
        createSut()

        sut updateWith Text("initial")

        uiData.text shouldBe Text("initial")
    }

    @Test
    fun `UiData update is not caused by user when text is changed by companion`() {
        createSut()

        sut updateWith Text("initial")

        uiDataUpdate.causedByUser shouldBe false
    }

    @Test
    fun `UiData text is updated when text is changed from UI`() {
        createSut()
        sut updateWith Text("initial")

        uiData.onTextChange(Text("new"))

        uiData.text shouldBe Text("new")
    }

    @Test
    fun `UiData update is caused by user when text is changed from UI`() {
        createSut()
        sut updateWith Text("initial")

        uiData.onTextChange(Text("new"))

        uiDataUpdate.causedByUser shouldBe true
    }

    @Test
    fun `trailing button is visible on initialization when text is non-empty`() {
        every { viewData.trailingIcon } returns mockk<TrailingIcon.Exists>(relaxed = true)
        createSut()

        sut updateWith Text("non-empty text")

        uiData.trailingButton should beOfType<TrailingButton.Visible>()
    }

    @Test
    fun `trailing button is hidden on initialization when text is empty`() {
        every { viewData.trailingIcon } returns mockk<TrailingIcon.Exists>(relaxed = true)
        createSut()

        sut updateWith Text("")

        uiData.trailingButton should beOfType<TrailingButton.Hidden>()
    }

    @Test
    fun `trailing button is visible when text is changed from UI and text is non-empty`() {
        every { viewData.trailingIcon } returns mockk<TrailingIcon.Exists>(relaxed = true)
        createSut()
        sut updateWith Text("initial")

        uiData.onTextChange(Text("non-empty text"))

        uiData.trailingButton should beOfType<TrailingButton.Visible>()
    }

    @Test
    fun `trailing button is hidden when text is changed from UI and text is empty`() {
        every { viewData.trailingIcon } returns mockk<TrailingIcon.Exists>(relaxed = true)
        createSut()
        sut updateWith Text("initial")

        uiData.onTextChange(Text(""))

        uiData.trailingButton should beOfType<TrailingButton.Hidden>()
    }

    @Test
    fun `trailing button is hidden when there should be no such`() {
        every { viewData.trailingIcon } returns TrailingIcon.None
        createSut()
        sut updateWith Text("initial")

        uiData.onTextChange(Text("any text"))

        uiData.trailingButton should beOfType<TrailingButton.Hidden>()
    }

    @Test
    fun `text is cleared on trailing button click`() {
        every { viewData.trailingIcon } returns mockk<TrailingIcon.Exists>(relaxed = true)
        createSut()
        sut updateWith Text("initial non-empty text")

        (uiData.trailingButton as TrailingButton.Visible).onClick()

        uiData.text shouldBe Text("")
    }

    @Test
    fun `UiData update is caused by user when trailing button is clicked`() {
        every { viewData.trailingIcon } returns mockk<TrailingIcon.Exists>(relaxed = true)
        createSut()
        sut updateWith Text("initial non-empty text")

        (uiData.trailingButton as TrailingButton.Visible).onClick()

        uiDataUpdate.causedByUser shouldBe true
    }

    fun createSut() =
        TextFieldViewModel(
            viewData = viewData,
            filterUserInput = { Text(it) },
        ).also {
            sut = it
        }
}