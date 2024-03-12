package io.github.mmolosay.thecolor.presentation.center

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test

class ColorCenterViewModelTest {

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
        ColorCenterViewModel().also {
            sut = it
        }

    val data: ColorCenterData
        get() = sut.dataFlow.value
}