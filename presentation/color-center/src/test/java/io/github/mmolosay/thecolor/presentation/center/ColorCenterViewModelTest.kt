package io.github.mmolosay.thecolor.presentation.center

import io.github.mmolosay.thecolor.testing.MainDispatcherExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class ColorCenterViewModelTest {

    val testDispatcher = UnconfinedTestDispatcher()

    @RegisterExtension
    @Suppress("unused")
    val mainDispatcherExtension = MainDispatcherExtension(testDispatcher)

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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun createSut() =
        ColorCenterViewModel(
            coroutineScope = CoroutineScope(context = testDispatcher),
            colorDetailsViewModel = mockk(),
            colorSchemeViewModel = mockk(),
        ).also {
            sut = it
        }

    val data: ColorCenterData
        get() = sut.dataFlow.value
}