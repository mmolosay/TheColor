package io.github.mmolosay.thecolor.presentation.api

import io.github.mmolosay.thecolor.presentation.api.nav.bar.NavBarAppearance
import io.github.mmolosay.thecolor.presentation.api.nav.bar.NavBarAppearanceControllerImpl
import io.github.mmolosay.thecolor.presentation.api.nav.bar.withTag
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NavBarAppearanceControllerImplTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    val testCoroutineContext = UnconfinedTestDispatcher()

    val testCoroutineScope = TestScope(testCoroutineContext)

    val sut = NavBarAppearanceControllerImpl(
        tag = "test",
        coroutineScope = testCoroutineScope,
    )

    @Test
    fun `pushing an appearance emits it from the flow`() {
        val appearance: NavBarAppearance.WithTag = mockk()

        sut.stack.push(appearance)

        sut.appearanceFlow.value shouldBe appearance
    }

    @Test
    fun `pushing two same appearances emits it from the flow only once`() =
        runTest(testCoroutineContext) {
            val emissions = mutableListOf<NavBarAppearance.WithTag?>()
            val collectionJob = launch {
                sut.appearanceFlow.toCollection(emissions)
            }

            val appearance: NavBarAppearance.WithTag = mockk()
            sut.stack.push(appearance)
            sut.stack.push(appearance)

            val expectedEmissions =
                listOf(null, appearance) // replayed initial 'null' and pushed appearance
            emissions shouldContainExactly expectedEmissions
            collectionJob.cancel()
        }

    @Test
    fun `peeling an appearance emits an appearance that was under it`() {
        val appearance1: NavBarAppearance.WithTag = mockk()
        val appearance2: NavBarAppearance.WithTag = mockk()

        sut.stack.push(appearance1)
        sut.stack.push(appearance2)
        sut.stack.peel()

        sut.appearanceFlow.value shouldBe appearance1
    }

    @Test
    fun `peeling single existing appearance emits 'null'`() {
        val appearance: NavBarAppearance.WithTag = mockk()

        sut.stack.push(appearance)
        sut.stack.peel()

        sut.appearanceFlow.value shouldBe null
    }

    @Test
    fun `peeling non-existant appearance does nothing`() {
        val appearance: NavBarAppearance.WithTag = mockk()

        sut.stack.push(appearance)
        sut.stack.peel()

        shouldNotThrowAny {
            sut.stack.peel()
        }
    }

    @Test
    fun `removing an appearance by tag when it is on top of the stack emits an appearance that was under it`() {
        val appearance1 = mockk<NavBarAppearance>() withTag "first"
        val appearance2 = mockk<NavBarAppearance>() withTag "second"

        sut.stack.push(appearance1)
        sut.stack.push(appearance2)
        sut.stack.remove(tag = "second")

        sut.appearanceFlow.value shouldBe appearance1
    }

    @Test
    fun `removing an appearance by tag when it is not on top of the stack doesn't emit anything`() =
        runTest(testCoroutineContext) {
            val emissions = mutableListOf<NavBarAppearance.WithTag?>()
            val collectionJob = launch(start = CoroutineStart.LAZY) {
                sut.appearanceFlow
                    .drop(1) // drop replayed value
                    .toCollection(emissions)
            }

            val appearance1 = mockk<NavBarAppearance>() withTag "first"
            val appearance2 = mockk<NavBarAppearance>() withTag "second"
            val appearance3 = mockk<NavBarAppearance>() withTag null
            sut.stack.push(appearance1)
            sut.stack.push(appearance2)
            sut.stack.push(appearance3)
            collectionJob.start()
            sut.stack.remove(tag = "second")

            emissions shouldHaveSize 0
            collectionJob.cancel()
        }

    @Test
    fun `removing an appearance by tag when there are multiple appearances with same tag only removes the latest one`() =
        runTest(testCoroutineContext) {
            val appearance1 = mockk<NavBarAppearance>() withTag "X"
            val appearance2 = mockk<NavBarAppearance>() withTag "Y"
            val appearance3 = mockk<NavBarAppearance>() withTag "Y"
            val appearance4 = mockk<NavBarAppearance>() withTag "Z"

            sut.stack.push(appearance1)
            sut.stack.push(appearance2)
            sut.stack.push(appearance3)
            sut.stack.push(appearance4)
            sut.stack.remove(tag = "Y")

            // "dig out" the appearance with the same tag
            sut.appearanceFlow.value shouldBe appearance4
            sut.stack.peel()
            sut.appearanceFlow.value shouldBe appearance2
        }

    @Test
    fun `removing an appearance by tag when there is no appearance with such tag does nothing`() {
        val appearance = mockk<NavBarAppearance>() withTag "X"
        sut.stack.push(appearance)

        shouldNotThrowAny {
            sut.stack.remove(tag = "Y")
        }
    }
}