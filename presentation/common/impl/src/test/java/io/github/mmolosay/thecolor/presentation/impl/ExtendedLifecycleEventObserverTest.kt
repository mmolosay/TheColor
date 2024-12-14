package io.github.mmolosay.thecolor.presentation.impl

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.github.mmolosay.thecolor.presentation.impl.ExtendedLifecycleEventObserver.LifecycleDirectionChangeEvent
import io.github.mmolosay.thecolor.presentation.impl.ExtendedLifecycleEventObserver.LifecycleDirectionChangeEvent.EnteringForeground
import io.github.mmolosay.thecolor.presentation.impl.ExtendedLifecycleEventObserver.LifecycleDirectionChangeEvent.LeavingForeground
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Tests [ExtendedLifecycleEventObserverAdapter] returned by
 * [ExtendedLifecycleEventObserver.toLifecycleEventObserver] function.
 */
@RunWith(Parameterized::class)
class ExtendedLifecycleEventObserverTest(
    val streamOfEvents: List<Lifecycle.Event>,
    val expectedDirectionChange: LifecycleDirectionChangeEvent?,
) {

    val lifecycleOwner: LifecycleOwner = mockk()
    val extendedObserver: ExtendedLifecycleEventObserver = mockk(relaxed = true)
    val sut = extendedObserver.toLifecycleEventObserver() // ExtendedLifecycleEventObserverAdapter

    fun LifecycleEventObserver.onStateChanged(event: Lifecycle.Event) =
        this.onStateChanged(source = lifecycleOwner, event = event)

    @Test
    fun `invoking observer with given events produces expected lifecycle direction change value`() {
        val streamOfEventsWithoutTheLastOne = streamOfEvents.dropLast(1)
        streamOfEventsWithoutTheLastOne.forEach { event ->
            sut.onStateChanged(event)
        }

        clearMocks(
            extendedObserver,
            answers = false,
            recordedCalls = true, // only clear recorded calls
            childMocks = false,
            verificationMarks = false,
            exclusionRules = false,
        )
        sut.onStateChanged(event = streamOfEvents.last())

        verify(exactly = 1) {
            extendedObserver.onStateChanged(
                source = any(),
                event = any(),
                directionChange = expectedDirectionChange,
            )
        }
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun testCases() = listOf(
            /* #0 */ listOf(ON_CREATE) resultsIn EnteringForeground,
            /* #1 */ listOf(ON_PAUSE) resultsIn LeavingForeground,
            /* #2 */ listOf(ON_RESUME, ON_PAUSE, ON_START) resultsIn EnteringForeground,
            /* #3 */ listOf(ON_RESUME, ON_PAUSE, ON_START, ON_RESUME) resultsIn null,
            /* #4 */ listOf(ON_RESUME, ON_PAUSE, ON_STOP, ON_DESTROY) resultsIn null,
        )

        infix fun List<Lifecycle.Event>.resultsIn(directionChange: LifecycleDirectionChangeEvent?) =
            arrayOf(this, directionChange)
    }
}