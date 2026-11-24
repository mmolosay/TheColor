package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.PrefState
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupCore
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupFeature
import io.github.mmolosay.thecolor.utils.composition.Store
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
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

    @Test
    fun `given the preferred input type setting is already 'Rgb', when the Feature is created, then 'Rgb' is the first of 'orderedTypes' without any coroutine running`() {
        val flowOfColorInputType = run {
            // 'Rgb' isn't the default value, so the test can't pass by falling back to it
            val value = DomainColorInputType.Rgb
            val result = PrefState.Result.HasValue(value)
            MutableStateFlow<PrefState<DomainColorInputType>>(PrefState.Ready(result))
        }
        every { userPreferencesRepository.flowOfColorInputType } returns flowOfColorInputType

        // its coroutines never run, so only a read made at creation can pass
        createSut(scope = CoroutineScope(Job() + StandardTestDispatcher()))

        val viewData = sut.viewData.value.shouldNotBeNull()
        viewData.orderedTypes.first() shouldBe DomainColorInputType.Rgb
    }

    fun createSut(
        scope: CoroutineScope = CoroutineScope(Job() + Dispatchers.Unconfined),
    ) =
        ColorInputGroupFeature(
            core = core,
            store = Store(initialState = core.initialState()),
            scope = scope,
            userPreferencesRepository = userPreferencesRepository,
        ).also {
            sut = it
        }
}
