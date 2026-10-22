package io.github.mmolosay.thecolor.composition

import io.github.mmolosay.thecolor.composition.CompositionUtils.launchAwaited
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

/**
 * Tests [CompositionUtils.launchAwaited].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineScopeExtLaunchAwaitedTest {

    val testDispatcher = UnconfinedTestDispatcher()

    val rye = Order(bread = "rye")
    val wheat = Order(bread = "wheat")

    val store = Store(initialState = Bakery(order = rye, other = 0), main = testDispatcher)

    val log = mutableListOf<String>()

    @Test
    fun `given the state awaits an order, when the work is launched, then it starts for that order`() =
        runTest(testDispatcher) {
            backgroundScope.launchAwaited(
                store = store,
                awaited = { it.order },
                block = { bake(it.bread) },
            )

            log shouldContainExactly listOf("started rye")
        }

    @Test
    fun `given the work is running, when another order is awaited, then the running work is cancelled and the work for the new order starts`() =
        runTest(testDispatcher) {
            backgroundScope.launchAwaited(
                store = store,
                awaited = { it.order },
                block = { bake(it.bread) },
            )

            store.update {
                Change(it.copy(order = wheat))
            }

            log shouldContainExactly listOf("started rye", "cancelled rye", "started wheat")
        }

    @Test
    fun `given the work is running, when the state changes but the awaited order stays the same instance, then the work is NOT restarted`() =
        runTest(testDispatcher) {
            backgroundScope.launchAwaited(
                store = store,
                awaited = { it.order },
                block = { bake(it.bread) },
            )

            store.update {
                Change(it.copy(other = 1)) // the same 'rye' instance stays in the state
            }

            log shouldContainExactly listOf("started rye")
        }

    @Test
    fun `given the work is running, when a new order for the same bread is awaited, then the work is restarted`() =
        runTest(testDispatcher) {
            backgroundScope.launchAwaited(
                store = store,
                awaited = { it.order },
                block = { bake(it.bread) },
            )

            store.update {
                Change(it.copy(order = Order(bread = "rye"))) // placed again, as a new order
            }

            log shouldContainExactly listOf("started rye", "cancelled rye", "started rye")
        }

    @Test
    fun `given the work is running for a data class, when a transition replaces it with an equal copy and changes something else, then the work is restarted`() =
        runTest(testDispatcher) {
            store.update {
                Change(it.copy(batch = Batch(bread = "rye")))
            }
            backgroundScope.launchAwaited(
                store = store,
                awaited = { it.batch },
                block = { bake(it.bread) },
            )

            store.update {
                // 'other' changes too, so that the store commits the state with the equal copy
                Change(it.copy(batch = it.batch?.copy(), other = 1))
            }

            log shouldContainExactly listOf("started rye", "cancelled rye", "started rye")
        }

    @Test
    fun `given the work is running, when nothing is awaited anymore, then the running work is cancelled and no new work starts`() =
        runTest(testDispatcher) {
            backgroundScope.launchAwaited(
                store = store,
                awaited = { it.order },
                block = { bake(it.bread) },
            )

            store.update {
                Change(it.copy(order = null))
            }

            log shouldContainExactly listOf("started rye", "cancelled rye")
        }

    suspend fun bake(bread: String) {
        log += "started $bread"
        try {
            awaitCancellation()
        } finally {
            log += "cancelled $bread"
        }
    }

    data class Bakery(
        val order: Order?,
        val other: Int,
        val batch: Batch? = null,
    )

    // not a data class: an order placed again must not equal the awaited one, or the store drops it
    class Order(
        val bread: String,
    )

    data class Batch(
        val bread: String,
    )
}
