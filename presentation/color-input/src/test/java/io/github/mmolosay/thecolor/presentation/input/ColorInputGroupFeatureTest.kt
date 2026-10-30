package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.PrefState
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupCore
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupFeature
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Test
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

class ColorInputGroupFeatureTest {

    val userPreferencesRepository: UserPreferencesRepository = mockk()

    // real implementation, operations on the State are pure
    val core = ColorInputGroupCore()

    lateinit var sut: ColorInputGroupFeature

    @Test
    fun `given the preferred input type setting is 'BeingInitialized', when it becomes 'Rgb', then 'Rgb' is the first of 'orderedTypes'`() {
        val flowOfColorInputType = run {
            val value = PrefState.BeingInitialized
            MutableStateFlow<PrefState<DomainColorInputType>>(value)
        }
        every { userPreferencesRepository.flowOfColorInputType } returns flowOfColorInputType
        createSut()

        // 'Rgb' isn't the default value, so the test can't pass by falling back to it
        flowOfColorInputType.value = run {
            val value = DomainColorInputType.Rgb
            val result = PrefState.Result.HasValue(value)
            PrefState.Ready(result)
        }

        val viewData = sut.viewData.value.shouldNotBeNull()
        viewData.orderedTypes.first() shouldBe DomainColorInputType.Rgb
    }

    fun createSut() =
        ColorInputGroupFeature(
            core = core,
            store = Store(initialState = core.initialState()),
            scope = CoroutineScope(Job() + Dispatchers.Unconfined),
            userPreferencesRepository = userPreferencesRepository,
        ).also {
            sut = it
        }
}
