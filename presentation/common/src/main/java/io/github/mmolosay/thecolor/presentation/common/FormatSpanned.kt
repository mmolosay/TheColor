package io.github.mmolosay.thecolor.presentation.common

import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.core.text.getSpans

/**
 * Formats receiver [Spanned] text using [String.format] with correct adjustment of all attached spans.
 */
fun Spanned.format(vararg args: Any): Spanned {
    val tree = this.toTree()
    val formatted = format(node = tree, *args)
    return formatted
}

private fun format(
    node: Node,
    vararg args: Any,
): Spanned {
    val builder = SpannableStringBuilder()
    if (node.children.isNotEmpty()) {
        for (child in node.children) {
            val formatted = format(child, *args)
            builder.append(formatted)
        }
    } else {
        val formatted = node.text.format(*args)
        builder.append(formatted)
    }
    if (node.span != null) {
        builder.setSpan(node.span, 0, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    return builder
}

private fun Spanned.toTree(): Node {
    val spans = this.getSpans<Any>()
        .map { span ->
            SpanAtPos(span = span, start = this.getSpanStart(span), end = this.getSpanEnd(span))
        }
        .sortedWith(
            compareBy<SpanAtPos> { it.start }.thenByDescending { it.end }
        )
    if (spans.isEmpty()) {
        return Node(span = null, text = this.toString(), children = emptyList())
    }
    fun textNode(start: Int, end: Int): Node? {
        if (start >= end) return null
        val text = this.substring(start, end)
        if (text.isEmpty()) return null
        return Node(span = null, text = text, children = emptyList())
    }
    fun build(start: Int, end: Int, span: Any?): Node {
        val spansInRange = spans
            .filterInRange(start, end)
            .removeSpan(span) // exclude itself to avoid infinite recursion
        val topLevelSpans = spansInRange.filterTopLevelSpans()
        val children = mutableListOf<Node>()
        var cursor = start
        for (span in topLevelSpans) {
            val textBeforeSpan = textNode(start = cursor, end = span.start)
            if (textBeforeSpan != null) children += textBeforeSpan

            children += build(span.start, span.end, span.span)
            cursor = span.end
        }
        if (topLevelSpans.isNotEmpty()) {
            val textAfterSpans = textNode(start = cursor, end = end)
            if (textAfterSpans != null) children += textAfterSpans
        }
        return Node(span = span, text = this.substring(start, end), children = children)
    }
    return build(start = 0, end = this.length, span = null)
}

private fun List<SpanAtPos>.filterInRange(start: Int, end: Int): List<SpanAtPos> =
    this.filter { span ->
        (span.start >= start) && (span.end <= end)
    }

private fun List<SpanAtPos>.removeSpan(span: Any?): List<SpanAtPos> {
    if (span == null) return this // receiver list doesn't contain nulls
    return this.toMutableList().also { spans ->
        spans.removeAll { it.span === span }
    }
}

/**
 * Returns a list of spans that would have been at the very top level
 * if the receiver spans were organized in a tree-like structure.
 *
 * Receiver list MUST be sorted by start ascending, and for equal starts by end descending.
 */
private fun List<SpanAtPos>.filterTopLevelSpans(): List<SpanAtPos> {
    val sortedSpans = this
    val topLevelSpans = mutableListOf<SpanAtPos>()
    var maxEnd = Int.MIN_VALUE
    for (span in sortedSpans) {
        if (span.end <= maxEnd) continue // contained or equal
        topLevelSpans += span
        maxEnd = span.end
    }
    return topLevelSpans
}

private data class SpanAtPos(
    val span: Any,
    val start: Int,
    val end: Int,
)

private data class Node(
    val span: Any?,
    val text: String,
    val children: List<Node>,
)