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
    fun `when a write changes the state, then the new state is committed with the next version`() {
        createSut()

        sut.update {
            Change(it.copy(other = 1))
        }

        sut.moment shouldBe Moment(initialState.copy(other = 1), version = 1)
    }

    @Test
    fun `when a write returns an equal state, then nothing is committed`() =
        runTest(testDispatcher) {
            createSut()

            sut.moments.test {
                skipItems(1) // replayed value of 'StateFlow'

                // WHEN
                sut.update {
                    Change(it.copy()) // a new instance, equal to the current one
                }

                // THEN
                sut.moment.version shouldBe 0
                expectNoEvents()
            }
        }

    @Test
    fun `when a write through a slice returns the state it was given, then nothing is committed`() {
        createSut()
        val letter = createLetterSlice()

        // as a dropped conditional change does; the desk's reducer still runs and builds an equal desk
        letter.update {
            Change(it)
        }

        sut.moment.version shouldBe 0
    }

    @Test
    fun `when a write goes through a slice, then the parent's reducer runs in the same update, so that the root commits once`() =
        runTest(testDispatcher) {
            createSut()
            val letter = createLetterSlice()

            sut.moments.test {
                skipItems(1) // replayed value of 'StateFlow'

                // WHEN
                letter.update {
                    Change(Letter(text = "A"))
                }

                // THEN
                awaitItem() shouldBe Moment(
                    initialState.copy(desk = Desk(letter = Letter(text = "A"), summary = "A")),
                    version = 1,
                )
            }
        }

    @Test
    fun `given nested slices, when a write hands up an outcome, then each parent's reducer handles the outcome of the one below it, in the same update`() {
        createSut()
        val letter = createLetterSlice()

        letter.update {
            Change(Letter(text = "A"), outcome = LetterOutcome.Sent)
        }

        sut.moment shouldBe Moment(
            initialState.copy(
                desk = Desk(letter = Letter(text = "A"), summary = "A"),
                sent = "A",
            ),
            version = 1,
        )
    }

    /**
     * Tests that a write through a slice and the reducers above it are one atomic update of the root:
     * if they were not, concurrent writes could overwrite each other, and some would be lost.
     */
    @RepeatedTest(10) // executed sequentially (by default)
    fun `given concurrent writes through different slices, when all of them return, then every write is applied with a version of its own`() =
        runTest(testDispatcher) {
            createSut()
            val letter = createLetterSlice()
            val other = createOtherSlice()
            val numberOfThreads = 16
            val writesPerThread = 100
            val dispatcher = Executors.newFixedThreadPool(numberOfThreads).asCoroutineDispatcher()
            val barrier = CyclicBarrier(numberOfThreads)

            dispatcher.use { dispatcher ->
                coroutineScope {
                    repeat(numberOfThreads) { index ->
                        launch(dispatcher) {
                            barrier.await()
                            repeat(writesPerThread) {
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

            val writesPerSlice = numberOfThreads / 2 * writesPerThread
            val text = "a".repeat(writesPerSlice)
            sut.moment shouldBe Moment(
                Office(desk = Desk(letter = Letter(text), summary = text), other = writesPerSlice),
                version = 2L * writesPerSlice,
            )
        }

    // endregion

    // region 'moment'

    /**
     * Tests that a slice's moment carries the version of the root's moment it was read from,
     * not the version of the commit that last changed the slice: a slice can't know that commit.
     * A View reads at once after its actions and then takes only newer versions,
     * so an older version here would let it take a late delivery that is older than what it shows.
     */
    @Test
    fun `given another part of the root changed after the slice's state, when the slice's 'moment' is read, then it carries the root's latest version`() {
        createSut()
        val letter = createLetterSlice()
        letter.update {
            Change(Letter(text = "A"))
        }
        sut.update {
            Change(it.copy(other = 1))
        }

        val moment = letter.moment

        moment shouldBe Moment(Letter(text = "A"), version = 2)
    }

    // endregion

    // region 'moments'

    @Test
    fun `when another part of the root changes and then the slice's state changes, then the slice's 'moments' emits only its own change, with the version of that commit`() =
        runTest(testDispatcher) {
            createSut()
            val letter = createLetterSlice()

            letter.moments.test {
                // WHEN
                sut.update {
                    Change(it.copy(other = 1))
                }
                letter.update {
                    Change(Letter(text = "A"))
                }

                // THEN
                // the current moment, when collecting starts
                awaitItem() shouldBe Moment(Letter(text = ""), version = 0)
                awaitItem() shouldBe Moment(Letter(text = "A"), version = 2)
            }
        }

    @Test
    fun `when the slice's state is replaced with an equal one, then the slice's 'moments' emits nothing new`() =
        runTest(testDispatcher) {
            createSut()
            val letter = createLetterSlice()

            letter.moments.test {
                skipItems(1) // the current moment, when collecting starts

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
        sut.slice(otherLens) { office, _: Nothing? -> Change(office) }

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
