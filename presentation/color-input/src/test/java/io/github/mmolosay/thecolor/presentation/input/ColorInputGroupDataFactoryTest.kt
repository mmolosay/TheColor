package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.PrefState
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupDataFactory
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

@OptIn(ExperimentalCoroutinesApi::class)
class ColorInputGroupDataFactoryTest {

    val testDispatcher = UnconfinedTestDispatcher()

    val userPreferencesRepository: UserPreferencesRepository = mockk()

    lateinit var sut: ColorInputGroupDataFactory

    @Test
    fun `given preferred input type is 'Rgb', when data is created, then 'Rgb' is the selected input type`() =
        runTest(testDispatcher) {
            every { userPreferencesRepository.flowOfColorInputType } returns run {
                val result = PrefState.Result.HasValue(DomainColorInputType.Rgb)
                val prefState = PrefState.Ready(result)
                MutableStateFlow(prefState)
            }
            createSut()

            val data = sut.create()

            data.selectedInputType shouldBe DomainColorInputType.Rgb
        }

    @Test
    fun `given preferred input type is 'Rgb', when data is created, then all input types are ordered with 'Rgb' being first`() =
        runTest(testDispatcher) {
            every { userPreferencesRepository.flowOfColorInputType } returns run {
                val result = PrefState.Result.HasValue(DomainColorInputType.Rgb)
                val prefState = PrefState.Ready(result)
                MutableStateFlow(prefState)
            }
            createSut()

            val data = sut.create()

            data.orderedInputTypes shouldContainExactlyInAnyOrder DomainColorInputType.entries
            data.orderedInputTypes.first() shouldBe DomainColorInputType.Rgb
        }

    @Test
    fun `given preferred input type is 'being initialized', when data is created, then SUT waits for the preference to be ready instead of using the default value`() =
        runTest(testDispatcher) {
            // 'Rgb' isn't the default value, so the test can't pass by falling back to it
            val flowOfColorInputType = run {
                val prefState = PrefState.BeingInitialized
                MutableStateFlow<PrefState<DomainColorInputType>>(prefState)
            }
            every { userPreferencesRepository.flowOfColorInputType } returns flowOfColorInputType
            createSut()

            val deferredData = async { sut.create() }
            run {
                val result = PrefState.Result.HasValue(DomainColorInputType.Rgb)
                val prefState = PrefState.Ready(result)
                flowOfColorInputType.emit(prefState)
            }

            deferredData.await().selectedInputType shouldBe DomainColorInputType.Rgb
        }

    fun createSut() =
        ColorInputGroupDataFactory(
            userPreferencesRepository = userPreferencesRepository,
        ).also {
            sut = it
        }
}
