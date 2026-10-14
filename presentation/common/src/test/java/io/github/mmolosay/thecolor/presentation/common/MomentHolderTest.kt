package io.github.mmolosay.thecolor.presentation.common

import androidx.compose.runtime.snapshots.Snapshot
import io.github.mmolosay.thecolor.composition.Moment
import io.github.mmolosay.thecolor.presentation.common.composition.MomentHolderImpl
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class MomentHolderTest {

    @Test
    fun `when a newer moment is offered, then its state is taken`() {
        val sut = MomentHolderImpl(initial = Moment(Shelf(book = "X"), version = 7))

        sut.offer(Moment(Shelf(book = "Y"), version = 8))

        sut.state.value shouldBe Shelf(book = "Y")
    }

    @Test
    fun `when a moment with an older version is offered, then it is ignored`() {
        val sut = MomentHolderImpl(initial = Moment(Shelf(book = "X"), version = 7))

        sut.offer(Moment(Shelf(book = "Y"), version = 6))

        sut.state.value shouldBe Shelf(book = "X")
    }

    @Test
    fun `when a newer moment with an equal state is offered, then the state is not written, so that nothing recomposes`() {
        val sut = MomentHolderImpl(initial = Moment(Shelf(book = "X"), version = 7))

        val changedStateObjects = changedStateObjectsOf {
            sut.offer(Moment(Shelf(book = "X"), version = 9)) // a new instance, equal to the held one
        }

        changedStateObjects shouldNotContain sut.state
    }

    @Test
    fun `given a newer moment with an equal state was offered, when a late moment with a version between the two is offered, then it is ignored`() {
        val sut = MomentHolderImpl(initial = Moment(Shelf(book = "X"), version = 7))
        sut.offer(Moment(Shelf(book = "X"), version = 9))

        sut.offer(Moment(Shelf(book = "Y"), version = 8))

        sut.state.value shouldBe Shelf(book = "X")
    }

    fun changedStateObjectsOf(block: () -> Unit): Set<Any> {
        val changed = mutableSetOf<Any>()
        val observer = Snapshot.registerApplyObserver { objects, _ -> changed.addAll(objects) }
        try {
            block()
            Snapshot.sendApplyNotifications() // as the recomposer does before each frame
        } finally {
            observer.dispose()
        }
        return changed
    }

    data class Shelf(
        val book: String,
    )
}
