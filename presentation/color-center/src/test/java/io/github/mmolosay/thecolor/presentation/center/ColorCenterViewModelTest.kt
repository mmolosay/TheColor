package io.github.mmolosay.thecolor.presentation.center

import io.kotest.matchers.shouldBe
import org.junit.Test

class ColorCenterViewModelTest {

    lateinit var sut: ColorCenterViewModel

    val data: ColorCenterData
        get() = sut.dataFlow.value

    @Test
    fun `change page action invoked with page '1' updates data accordingly`() {
        createSut()

        data.changePage(destPage = 1)

        data.page shouldBe 1
    }

    @Test
    fun `on page changed action invoked with page '1' updates data accordingly`() {
        createSut()

        data.onPageChanged(newPage = 1)

        data.page shouldBe 1
    }

    fun createSut() =
        ColorCenterViewModel().also {
            sut = it
        }
}