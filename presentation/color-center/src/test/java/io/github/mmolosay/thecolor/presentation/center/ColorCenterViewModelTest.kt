package io.github.mmolosay.thecolor.presentation.center

import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import kotlinx.coroutines.test.TestScope
import org.junit.Rule
import org.junit.Test

class ColorCenterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    lateinit var sut: ColorCenterViewModel

    @Test
    fun `'change page' action invoked with page '1' updates data new event`() {
        createSut()

        data.changePage(1)

        data.changePageEvent shouldNotBe null
    }


    @Test
    fun `'change page' action invoked with page '1' updates data with proper event`() {
        createSut()

        data.changePage(1)

        data.changePageEvent?.destPage shouldBe 1
    }

    @Test
    fun `consuming 'change page event' removes the event from data`() {
        createSut()
        // bring SUT to initial state with data that contains an event
        data.changePage(1)

        data.changePageEvent?.onConsumed?.invoke()

        data.changePageEvent shouldBe null
    }

    fun createSut() =
        ColorCenterViewModel(
            coroutineScope = TestScope(context = mainDispatcherRule.testDispatcher),
            colorCenterCommandProvider = mockk(),
            colorCenterEventStore = mockk(),
            colorDetailsViewModelFactory = { _, _, _ -> mockk() },
            colorSchemeViewModelFactory = { _, _ -> mockk() },
        ).also {
            sut = it
        }

    val data: ColorCenterData
        get() = sut.dataFlow.value
}