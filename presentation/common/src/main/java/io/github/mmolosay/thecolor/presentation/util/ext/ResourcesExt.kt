package io.github.mmolosay.thecolor.presentation.util.ext

import android.content.res.Resources
import android.text.Annotation
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.SpannedString
import androidx.annotation.StringRes
import androidx.core.text.getSpans
import io.github.mmolosay.thecolor.presentation.R
import kotlin.math.min

private const val STRING_RESOURCE_ANNOTATION_KEY = "ordinal"

fun Resources.getStringYesOrNo(yes: Boolean): String {
    return if (yes) {
        getString(R.string.generic_yes)
    } else {
        getString(R.string.generic_no)
    }
}

fun Resources.getStringOrNull(res: Int): String? =
    Result.runCatching {
        getString(res)
    }.getOrNull()

/**
 * Retrieves string from [Resources] by its [resid] and applies specified [spans] to it.
 * String should containt `<annotation>` tag with [STRING_RESOURCE_ANNOTATION_KEY] as key
 * and Integer as value.
 * For example:
 * ```
 *      <string>This <annotation ordinal="0">is</annotation> a <annotation ordinal="1">string</annotation></string>
 * ```
 */
fun Resources.getStringWithAnnotations(
    @StringRes resid: Int,
    spans: List<Any>
): Spanned {
    val spanned = this.getText(resid) as SpannedString
    val annotations = spanned
        .getSpans<Annotation>()
        .filter { it.key == STRING_RESOURCE_ANNOTATION_KEY }
        .sortedBy { it.value.toIntOrNull() }
    val builder = SpannableStringBuilder(spanned)
    val count = min(annotations.size, spans.size)
    for (i in 0 until count) {
        val span = spans[i]
        val annotation = annotations[i]
        val start = spanned.getSpanStart(annotation)
        val end = spanned.getSpanEnd(annotation)
        builder.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    return builder
}