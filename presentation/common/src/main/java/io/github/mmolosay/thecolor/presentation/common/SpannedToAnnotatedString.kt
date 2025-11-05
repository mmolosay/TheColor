package io.github.mmolosay.thecolor.presentation.common

import android.text.Spanned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.core.text.getSpans
import io.github.mmolosay.thecolor.utils.doNothing

/**
 * Converts this [Spanned] (from Android SDK) into an [AnnotatedString] (from Compose).
 */
inline fun <reified T : Any> Spanned.toAnnotatedString(
    convertSpanToAnnotation: SpanToAnnotationConverter<T>,
): AnnotatedString =
    buildAnnotatedString {
        val spanned = this@toAnnotatedString
        append(spanned) // won't copy Android Spans (as per documentation)
        val spans = spanned.getSpans<T>()
        for (span in spans) {
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            val annotation = convertSpanToAnnotation(span)
            when (annotation) {
                is SpanStyle -> addStyle(annotation, start, end)
                is ParagraphStyle -> addStyle(annotation, start, end)
                is LinkAnnotation.Clickable -> addLink(annotation, start, end)
                is LinkAnnotation.Url -> addLink(annotation, start, end)
                null -> doNothing() // this Span cannot be converted to Annotation, so just skip it
                else -> error("span $span was converted to unsupported annotation $annotation")
            }
        }
    }

/**
 * Converts a span (from Android SDK) of type [T] into an [AnnotatedString.Annotation] (from Compose).
 */
// must be a 'typealias' instead of 'fun interface' to enable inlining (for Composable calls).
private typealias SpanToAnnotationConverter<T> =
    (span: T) -> AnnotatedString.Annotation?