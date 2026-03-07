package io.github.mmolosay.thecolor.presentation.input.testing

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
    answer: suspend (color: Color, source: DomainColorInputType?) -> Unit,
) {
    val editor = this
    every {
        editor.set(color = any(), source = any())
    } coAnswers {
        val color = firstArg<Color>()
        val source = secondArg<DomainColorInputType?>()
        answer.invoke(color, source)
    }
}