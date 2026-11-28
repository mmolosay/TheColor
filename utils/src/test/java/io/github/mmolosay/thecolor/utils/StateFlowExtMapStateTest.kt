package io.github.mmolosay.thecolor.utils

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StateFlowExtMapStateTest {

    val testDispatcher = UnconfinedTestDispatcher()

    val source = MutableStateFlow(Lamp(isOn = false, watts = 40))

    val sut = source.mapState { lamp -> lamp.isOn }

    @Test
    fun `when the source's value changes, then SUT's 'value' is the new result at once, so that it never lags behind the source as one made with 'stateIn' does`() {
        source.value = Lamp(isOn = true, watts = 40)

        sut.value shouldBe true
    }

    @Test
    fun `when the source's value changes and the result stays equal, then SUT emits nothing new`() =
        runTest(testDispatcher) {
            val emitted = mutableListOf<Boolean>()
            backgroundScope.launch { sut.toList(emitted) }

            source.value = Lamp(isOn = false, watts = 60)

            emitted shouldBe listOf(false) // only the value replayed when the collection started
        }

    data class Lamp(
        val isOn: Boolean,
        val watts: Int,
    )
}
