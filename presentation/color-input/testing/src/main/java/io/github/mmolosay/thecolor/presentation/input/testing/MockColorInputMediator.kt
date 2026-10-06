package io.github.mmolosay.thecolor.presentation.input.testing

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator.ColorState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.util.concurrent.atomic.AtomicLong

fun MockColorInputMediatorComponents(): MockColorInputMediatorComponents {
    val editor = mockk<ColorInputMediator.Editor>(relaxed = true)
    val nextRevisionValue = AtomicLong(1) // after the one of 'EmptyColorState'
    val mediator = mockk<ColorInputMediator>(relaxed = true) {
        every { newRevision() } answers { ColorState.Revision(nextRevisionValue.getAndIncrement()) }
        coEvery { withLock<Any?>(block = any()) } coAnswers {
            val block = firstArg<suspend (ColorInputMediator.Editor) -> Any?>()
            block.invoke(editor)
        }
    }
    return MockColorInputMediatorComponents(
        mediator = mediator,
        editor = editor,
    )
}

data class MockColorInputMediatorComponents(
    val mediator: ColorInputMediator,
    val editor: ColorInputMediator.Editor,
)

/**
 * Mocks [ColorInputMediator.Editor.set] method.
 */
fun ColorInputMediator.Editor.mockSet(
    answer: suspend (color: Color?, source: ColorState.Source?, revision: ColorState.Revision) -> Boolean,
) {
    val editor = this
    val slotOfColor = slot<Color?>()
    val slotOfSource = slot<ColorState.Source?>()
    val slotOfRevision = slot<ColorState.Revision>()
    every {
        editor.set(
            color = captureNullable(slotOfColor),
            source = captureNullable(slotOfSource),
            revision = capture(slotOfRevision),
        )
    } coAnswers set@{
        val color = slotOfColor.captured
        val source = slotOfSource.captured
        val revision = slotOfRevision.captured
        return@set answer.invoke(color, source, revision)
    }
}

val EmptyColorState =
    ColorState(color = null, source = null, revision = ColorState.Revision(0))