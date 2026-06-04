package io.github.mmolosay.thecolor.utils

import io.github.mmolosay.thecolor.utils.OpCounter.OnStateChangeListener
import io.github.mmolosay.thecolor.utils.OpCounter.State
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class OpCounterTest {

    @Test
    fun `when counter is updated, then it is reflected in 'total count flow'`() {
        val counter = OpCounter()

        counter.update { it + 1 }

        counter.state.totalCount shouldBe 1
    }

    @Test
    fun `when counter is updated, then it is reflected in 'total count flow' of the parent`() {
        val child = OpCounter(name = "1")
        val parent = OpCounter(name = "2", child)

        child.update { it + 1 }

        parent.state.totalCount shouldBe 1
    }

    @Test
    fun `when counter is updated, then it is reflected in 'total count flow' of the top-most parent`() {
        val counter1 = OpCounter(name = "1")
        val counter2 = OpCounter(name = "2", counter1)
        val counter3 = OpCounter(name = "3", counter2)

        counter1.update { it + 1 }

        counter3.state.totalCount shouldBe 1
    }

    @Test
    fun `given the listener is specified, when counter is updated, then the listener receives the updated State`() {
        var latestStateFromListener: State? = null
        val listener = OnStateChangeListener { newState ->
            latestStateFromListener = newState
        }
        val counter = OpCounter().apply {
            addListener(listener)
        }

        counter.update { it + 1 }

        latestStateFromListener.shouldNotBeNull().totalCount shouldBe 1
    }
}