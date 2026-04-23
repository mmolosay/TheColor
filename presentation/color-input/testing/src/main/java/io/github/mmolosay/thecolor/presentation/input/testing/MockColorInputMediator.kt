package io.github.mmolosay.thecolor.presentation.input.testing

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

fun MockColorInputMediatorComponents(): MockColorInputMediatorComponents {
    val editor = mockk<ColorInputMediator.Editor>(relaxed = true)
    val mediator = mockk<ColorInputMediator>(relaxed = true) {
        coEvery { withLock(block = any()) } coAnswers {
            val block = firstArg<suspend (ColorInputMediator.Editor) -> Unit>()
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
    answer: suspend (color: Color?, source: DomainColorInputType?) -> Unit,
) {
    val editor = this
    val slotOfColor = slot<Color?>()
    val slotOfSource = slot<DomainColorInputType?>()
    every {
        editor.set(color = captureNullable(slotOfColor), source = captureNullable(slotOfSource))
    } coAnswers {
        val color = slotOfColor.captured
        val source = slotOfSource.captured
        answer.invoke(color, source)
    }
}