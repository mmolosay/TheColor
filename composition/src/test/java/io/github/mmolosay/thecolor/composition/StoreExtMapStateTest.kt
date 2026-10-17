package io.github.mmolosay.thecolor.composition

import app.cash.turbine.test
import io.github.mmolosay.thecolor.utils.Lens
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StoreExtMapStateTest {

    val testDispatcher = UnconfinedTestDispatcher()

    val store = MutableStore(initialState = Room(lamp = Lamp(isOn = false, watts = 40), other = 0))

    // a slice, so that a commit can leave its state the same instance
    val source = store.slice(
        lens = Lens<Room, Lamp>(
            get = { it.lamp },
            set = { room, lamp -> room.copy(lamp = lamp) },
        ),
        reduce = { room, _: Nothing? -> Change(room) },
    )

    var transformRuns = 0

    val sut = source.mapState { lamp ->
        transformRuns++
        lamp.isOn
    }

    @Test
    fun `when the source's state changes, then SUT's 'value' is the new result at once, so that the result of an action can be read as soon as the action returns`() {
        source.update {
            Change(it.copy(isOn = true))
        }

        sut.value shouldBe Moment(true, version = 1)
    }

    @Test
    fun `when the source's state changes and the result stays equal, then SUT emits the result with the version of that commit, so that its collectors end on SUT's 'value'`() =
        runTest(testDispatcher) {
            sut.test {
                skipItems(1) // replayed value of 'StateFlow'

                // WHEN
                source.update {
                    Change(it.copy(watts = 60))
                }

                // THEN
                awaitItem() shouldBe Moment(false, version = 1)
            }
        }

    @Test
    fun `when a commit leaves the source's state the same instance, then 'transform' is NOT run again for it`() =
        runTest(testDispatcher) {
            sut.test {
                skipItems(1) // replayed value of 'StateFlow'

                // WHEN
                store.update {
                    Change(it.copy(other = 1)) // the lamp stays the same instance
                }
                awaitItem() // the moment of that commit

                // THEN
                transformRuns shouldBe 1 // for the moment replayed when the collection started
            }
        }

    data class Room(
        val lamp: Lamp,
        val other: Int,
    )

    data class Lamp(
        val isOn: Boolean,
        val watts: Int,
    )
}
