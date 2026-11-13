package io.github.mmolosay.thecolor.presentation.common.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.KeyInputModifierNode
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalFocusManager

/**
 * On [Key.Backspace] press, moves the focus to the [FocusDirection.Previous] focusable element
 * (presumably, a text field) if the [text] (of the currently focused text field) is empty.
 */
fun Modifier.smartBackspace(
    text: () -> String,
): Modifier =
    this then SmartBackspaceElement(text)

private data class SmartBackspaceElement(
    private val text: () -> String,
) : ModifierNodeElement<SmartBackspaceNode>() {

    override fun create(): SmartBackspaceNode =
        SmartBackspaceNode(text)

    override fun update(node: SmartBackspaceNode) {
        node.text = this.text
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "smartBackspace"
        properties["text"] = text
    }
}

private class SmartBackspaceNode(
    var text: () -> String,
) : Modifier.Node(),
    KeyInputModifierNode,
    CompositionLocalConsumerModifierNode {

    override fun onPreKeyEvent(event: KeyEvent): Boolean {
        if (event.key != Key.Backspace) return false
        if (event.type != KeyEventType.KeyDown) return false
        if (text().isNotEmpty()) return false
        val focusManager = currentValueOf(LocalFocusManager)
        return focusManager.moveFocus(FocusDirection.Previous)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean =
        false
}