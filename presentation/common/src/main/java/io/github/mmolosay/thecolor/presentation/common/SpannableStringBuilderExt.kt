package io.github.mmolosay.thecolor.presentation.common

import android.text.Annotation
import android.text.SpannableStringBuilder
import androidx.core.text.inSpans

/**
 * Wrap appended text in [builderAction] in a [Annotation] with provided [key] and [value].
 *
 * @see SpannableStringBuilder.inSpans
 */
inline fun SpannableStringBuilder.annotation(
    key: String,
    value: String,
    builderAction: SpannableStringBuilder.() -> Unit,
): SpannableStringBuilder =
    inSpans(span = Annotation(key, value), builderAction = builderAction)