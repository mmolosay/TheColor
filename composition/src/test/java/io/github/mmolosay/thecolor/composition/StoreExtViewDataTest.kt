package io.github.mmolosay.thecolor.composition

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.Test

/**
 * Tests [Store.viewData].
 */
class StoreExtViewDataTest {

    val store = Store(initialState = Shelf(book = "X"), main = UnconfinedTestDispatcher())

    @Test
    fun `given the store's state is the same instance, when the view data is read again, then 'toViewData' is NOT run again, so that a commit elsewhere costs a View one comparison`() {
        var runs = 0
        val sut = store.viewData { shelf ->
            runs++
            shelf.book
        }
        sut.value

        sut.value

        runs shouldBe 1
    }

    data class Shelf(
        val book: String,
    )
}
