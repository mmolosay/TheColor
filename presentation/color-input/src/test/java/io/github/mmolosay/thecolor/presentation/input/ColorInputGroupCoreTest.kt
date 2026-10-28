package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupCore
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

class ColorInputGroupCoreTest {

    val sut = ColorInputGroupCore()

    @Test
    fun `when 'applyPreferredType' is invoked, then the state is initialized`() {
        val state = sut.initialState()

        val applied = with(sut) { state.applyPreferredType(DomainColorInputType.Rgb) }.state

        with(sut) { applied.isInitialized() } shouldBe true
    }

    @Test
    fun `when 'applyPreferredType' is invoked with 'Rgb', then 'Rgb' is the first of 'orderedTypes' and the 'selectedType'`() {
        val state = sut.initialState()

        val applied = with(sut) { state.applyPreferredType(DomainColorInputType.Rgb) }.state

        val viewData = with(sut) { applied.toViewData() }.shouldNotBeNull()
        viewData.orderedTypes shouldContainExactlyInAnyOrder DomainColorInputType.entries
        viewData.orderedTypes.first() shouldBe DomainColorInputType.Rgb
        viewData.selectedType shouldBe DomainColorInputType.Rgb
    }

    @Test
    fun `given the user selected 'Hsv', when 'applyPreferredType' is invoked with 'Rgb', then 'selectedType' stays 'Hsv'`() {
        val state = with(sut) { initialState().select(DomainColorInputType.Hsv) }.state

        val applied = with(sut) { state.applyPreferredType(DomainColorInputType.Rgb) }.state

        val viewData = with(sut) { applied.toViewData() }.shouldNotBeNull()
        viewData.selectedType shouldBe DomainColorInputType.Hsv
    }
}
