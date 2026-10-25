package io.github.mmolosay.thecolor.composition

import app.cash.turbine.test
import io.github.mmolosay.thecolor.utils.Lens
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors

/**
 * Tests [Store] created with `Store(initialState)`, and its slices.
 *
 * The states model an office with a desk, and a letter on the desk.
 * The desk's reducer keeps its `summary` equal to the letter's text, and turns the letter's
 * [LetterOutcome.Sent] into its own [DeskOutcome.Sent].
 * The office's reducer records the sent text.
 * The names are made up, so that they don't mix with the app's features.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StoreTest {

    val testDispatcher = UnconfinedTestDispatcher()

    val initialState = Office(
        desk = Desk(letter = Letter(text = ""), summary = ""),
        other = 0,
    )

    lateinit var sut: Store<Office, Nothing>

    // region 'update'

    @Test
    fun `when a transition changes the state, then the new state is committed`() {
        createSut()

        sut.update {
            Change(it.copy(other = 1))
        }

        sut.value shouldBe initialState.copy(other = 1)
    }

    @Test
    fun `when a transition returns an equal state, then nothing is committed`() =
        runTest(testDispatcher) {
            createSut()

            sut.test {
                skipItems(1) // replayed value of 'StateFlow'

                // WHEN
                sut.update {
                    Change(it.copy()) // a new instance, equal to the current one
                }

                // THEN
                expectNoEvents()
            }
        }

    @Test
    fun `when a transition through a slice returns an equal state and no outcome, then the parent's reducer does NOT run, so that nothing is committed`() {
        createSut()
        val desk = sut.slice(deskLens) { office ->
            Change(office.copy(other = office.other + 1)) // changes the office whenever it runs
        }

        desk.update {
            Change(it.copy()) // a new instance, equal to the one given
        }

        sut.value shouldBe initialState
    }

    @Test
    fun `when a transition through a slice returns an equal state with an outcome, then the parent's reducer handles the outcome`() {
        createSut()
        val letter = createLetterSlice()

        letter.update {
            Change(it, outcome = LetterOutcome.Sent)
        }

        sut.value shouldBe initialState.copy(sent = "")
    }

    @Test
    fun `when a transition goes through a slice, then the parent's reducer runs in the same update, so that the root commits once`() =
        runTest(testDispatcher) {
            createSut()
            val letter = createLetterSlice()

            sut.test {
                skipItems(1) // replayed value of 'StateFlow'

                // WHEN
                letter.update {
                    Change(Letter(text = "A"))
                }

                // THEN
                // the first state after the transition already has the reducer's change
                awaitItem() shouldBe initialState.copy(
                    desk = Desk(letter = Letter(text = "A"), summary = "A"),
                )
            }
        }

    @Test
    fun `given nested slices, when a transition hands up an outcome, then each parent's reducer handles the outcome of the one below it, in the same update`() {
        createSut()
        val letter = createLetterSlice()

        letter.update {
            Change(Letter(text = "A"), outcome = LetterOutcome.Sent)
        }

        sut.value shouldBe initialState.copy(
            desk = Desk(letter = Letter(text = "A"), summary = "A"),
            sent = "A",
        )
    }

    /**
     * Tests that a transition through a slice and the reducers above it are one atomic update of the
     * root: if they were not, concurrent updates could overwrite each other, and some would be lost.
     */
    @RepeatedTest(10) // executed sequentially (by default)
    fun `given concurrent updates through different slices, when all of them return, then every transition is applied`() =
        runTest(testDispatcher) {
            createSut()
            val letter = createLetterSlice()
            val other = createOtherSlice()
            val numberOfThreads = 16
            val updatesPerThread = 100
            val dispatcher = Executors.newFixedThreadPool(numberOfThreads).asCoroutineDispatcher()
            val barrier = CyclicBarrier(numberOfThreads)

            dispatcher.use { dispatcher ->
                coroutineScope {
                    repeat(numberOfThreads) { index ->
                        launch(dispatcher) {
                            barrier.await()
                            repeat(updatesPerThread) {
                                if (index % 2 == 0) {
                                    letter.update { state ->
                                        Change(Letter(text = state.text + "a"))
                                    }
                                } else {
                                    other.update { state ->
                                        Change(state + 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val updatesPerSlice = numberOfThreads / 2 * updatesPerThread
            val text = "a".repeat(updatesPerSlice)
            sut.value shouldBe Office(
                desk = Desk(letter = Letter(text), summary = text),
                other = updatesPerSlice,
            )
        }

    // endregion

    // region 'collect'

    @Test
    fun `when the root commits and the slice's state stays equal, then the slice emits nothing new, so that a commit elsewhere reaches none of its collectors`() =
        runTest(testDispatcher) {
            createSut()
            val letter = createLetterSlice()

            letter.test {
                skipItems(1) // replayed value of 'StateFlow'

                // WHEN
                sut.update {
                    val equalDesk = it.desk.copy(letter = it.desk.letter.copy())
                    // 'other' changes too, so that the root commits and the slice has something to filter
                    Change(it.copy(desk = equalDesk, other = 1))
                }

                // THEN
                expectNoEvents()
            }
        }

    // endregion

    fun createSut() =
        Store(initialState).also {
            sut = it
        }

    fun createLetterSlice(): Store<Letter, LetterOutcome> =
        sut
            .slice(deskLens, ::onDesk)
            .slice(letterLens, ::onLetter)

    fun createOtherSlice(): Store<Int, Nothing> =
        sut.slice(otherLens) { office -> Change(office) }

    fun onDesk(office: Office, outcome: DeskOutcome?): Change<Office, Nothing> =
        when (outcome) {
            is DeskOutcome.Sent -> Change(office.copy(sent = outcome.text))
            null -> Change(office)
        }

    fun onLetter(desk: Desk, outcome: LetterOutcome?): Change<Desk, DeskOutcome> {
        val inLine = desk.copy(summary = desk.letter.text)
        return when (outcome) {
            LetterOutcome.Sent -> Change(inLine, outcome = DeskOutcome.Sent(inLine.summary))
            null -> Change(inLine)
        }
    }

    val deskLens = Lens<Office, Desk>(
        get = { it.desk },
        set = { source, value -> source.copy(desk = value) },
    )

    val letterLens = Lens<Desk, Letter>(
        get = { it.letter },
        set = { source, value -> source.copy(letter = value) },
    )

    val otherLens = Lens<Office, Int>(
        get = { it.other },
        set = { source, value -> source.copy(other = value) },
    )

    data class Office(
        val desk: Desk,
        val other: Int,
        val sent: String? = null,
    )

    data class Desk(
        val letter: Letter,
        val summary: String,
    )

    data class Letter(
        val text: String,
    )

    sealed interface LetterOutcome {
        data object Sent : LetterOutcome
    }

    sealed interface DeskOutcome {
        data class Sent(val text: String) : DeskOutcome
    }
}
