package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupAction
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupData
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupViewModel
import io.github.mmolosay.thecolor.testing.MainDispatcherExtension
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

@OptIn(ExperimentalCoroutinesApi::class)
class ColorInputGroupViewModelTest {

    val testDispatcher = UnconfinedTestDispatcher()

    // can't be unconfined, because SUT derives a dispatcher with limited parallelism from it
    val defaultDispatcher = StandardTestDispatcher(testDispatcher.scheduler)

    @RegisterExtension
    @Suppress("unused")
    val mainDispatcherExtension = MainDispatcherExtension(testDispatcher)

    val mediator: ColorInputMediator = mockk()

    lateinit var sut: ColorInputGroupViewModel

    @Test
    fun `when 'ChangeInputType' action is executed, then data has the new 'selected input type'`() =
        runTest(testDispatcher) {
            val initialData = ColorInputGroupData(
                selectedInputType = DomainColorInputType.Hex,
                orderedInputTypes = DomainColorInputType.entries,
            )
            createSut(initialData = initialData)

            run {
                val action = ColorInputGroupAction.ChangeInputType(DomainColorInputType.Rgb)
                sut.execute(action).join()
            }

            data.selectedInputType shouldBe DomainColorInputType.Rgb
        }

    fun createSut(
        initialData: ColorInputGroupData,
    ) =
        ColorInputGroupViewModel(
            coroutineScope = CoroutineScope(context = testDispatcher),
            initialState = initialData,
            mediator = mediator,
            submitAction = mockk(),
            hexViewModelFactory = { _, _, _ -> mockk(relaxed = true) },
            rgbViewModelFactory = { _, _, _ -> mockk(relaxed = true) },
            hsvViewModelFactory = { _, _ -> mockk(relaxed = true) },
            defaultDispatcher = defaultDispatcher,
        ).also {
            sut = it
        }

    val data: ColorInputGroupData
        get() = sut.dataFlow.value
}
