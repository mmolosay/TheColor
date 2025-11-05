package io.github.mmolosay.thecolor.presentation.common

import android.os.Build
import android.text.Annotation
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.getSpans
import androidx.core.text.italic
import io.github.mmolosay.thecolor.utils.doNothing
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.char.beEqualIgnoreCase
import io.kotest.matchers.ints.beLessThan
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.robolectric.annotation.Config
import tech.apter.junit.jupiter.robolectric.RobolectricExtension
import kotlin.comparisons.compareBy

/**
 * Tests [Spanned.format] extension.
 */
@ExtendWith(RobolectricExtension::class)
@Config(
    sdk = [Build.VERSION_CODES.TIRAMISU],
)
class FormatSpannedTest {

    @ParameterizedTest
    @MethodSource("data")
    fun `formatting a template Spanned with specified format args produces expected Spanned`(
        templateSpanned: Spanned,
        formatArgs: Array<Any>,
        expectedSpanned: Spanned,
    ) {
        val result = templateSpanned.format(*formatArgs)
        result should beDeepEqualTo(expectedSpanned)
    }

    companion object {

        @JvmStatic
        fun data(): Array<Array<Any>> = listOf(
            /* #0 */
            TestCase(
                templateSpanned = buildSpannedString {
                    append("Hello world")
                },
                formatArgs = arrayOf(),
                expectedSpanned = buildSpannedString {
                    append("Hello world")
                },
            ),
            /* #1 */
            TestCase(
                templateSpanned = buildSpannedString {
                    bold { append("Hello") }
                    append(" ")
                    append("%1\$s")
                    append(" ")
                    append("world")
                },
                formatArgs = arrayOf("beautiful"),
                expectedSpanned = buildSpannedString {
                    bold { append("Hello") }
                    append(" beautiful world")
                },
            ),
            /* #2 */
            TestCase(
                templateSpanned = buildSpannedString {
                    bold { append("Hello") }
                    append(" ")
                    bold { append("%1\$s") }
                },
                formatArgs = arrayOf("world"),
                expectedSpanned = buildSpannedString {
                    bold { append("Hello") }
                    append(" ")
                    bold { append("world") }
                },
            ),
            /* #3 */
            TestCase(
                templateSpanned = buildSpannedString {
                    bold { append("%1\$s") }
                    append(" ")
                    bold { append("%2\$s") }
                },
                formatArgs = arrayOf("Hello", "world"),
                expectedSpanned = buildSpannedString {
                    bold { append("Hello") }
                    append(" ")
                    bold { append("world") }
                },
            ),
            /* #4 */
            TestCase(
                templateSpanned = buildSpannedString {
                    italic {
                        bold { append("%1\$s") }
                        append(" ")
                    }
                    bold { append("%2\$s") }
                },
                formatArgs = arrayOf("Hello", "world"),
                expectedSpanned = buildSpannedString {
                    italic {
                        bold { append("Hello") }
                        append(" ")
                    }
                    bold { append("world") }
                },
            ),
            /* #5 */
            TestCase(
                templateSpanned = buildSpannedString {
                    italic {
                        bold { append("%1\$s") }
                        append(" world")
                    }
                },
                formatArgs = arrayOf("Hello"),
                expectedSpanned = buildSpannedString {
                    italic {
                        bold { append("Hello") }
                        append(" world")
                    }
                },
            ),
        )
            .map { it.asArrayOfAnys() }
            .toTypedArray()

        @Suppress("ArrayInDataClass")
        data class TestCase(
            val templateSpanned: Spanned,
            val formatArgs: Array<Any>,
            val expectedSpanned: Spanned,
        )

        fun TestCase.asArrayOfAnys(): Array<Any> =
            arrayOf(templateSpanned, formatArgs, expectedSpanned)
    }
}

fun beDeepEqualTo(other: Spanned) = object : Matcher<Spanned> {
    override fun test(value: Spanned) =
        MatcherResult(
            passed = value.deepEquals(other),
            failureMessageFn = { "\"$value\" should have the same text and spans as \"$other\"" },
            negatedFailureMessageFn = { error("negated message is not defined") },
        )
}

fun Spanned.deepEquals(that: Spanned): Boolean {
    if (this.toString() != that.toString()) return false
    fun Spanned.getSortedSpans() =
        this.getSpans<Any>().sortedWith(
            compareBy(
                { span -> this.getSpanStart(span) },
                { span -> this.getSpanEnd(span) },
                { span -> span::javaClass.name },
            )
        )
    val spans1 = this.getSortedSpans()
    val spans2 = that.getSortedSpans()
    if (spans1.size != spans2.size) return false
    val zippedSpans = spans1.zip(spans2)
    for ((span1, span2) in zippedSpans) {
        if (span1.javaClass != span2.javaClass) return false
        if (this.getSpanStart(span1) != that.getSpanStart(span2)) return false
        if (this.getSpanEnd(span1) != that.getSpanEnd(span2)) return false
        when (span1) {
            is StyleSpan -> {
                require(span2 is StyleSpan)
                if (!span1.isEqualTo(span2)) return false
            }
            is Annotation -> {
                require(span2 is Annotation)
                if (!span1.isEqualTo(span2)) return false
            }
            else -> doNothing() // equality check for other types is not needed thus not defined
        }
    }
    return true
}

private fun StyleSpan.isEqualTo(that: StyleSpan): Boolean {
    if (this.style != that.style) return false
    if (this.fontWeightAdjustment != that.fontWeightAdjustment) return false
    return true
}

private fun Annotation.isEqualTo(that: Annotation): Boolean {
    if (this.key != that.key) return false
    if (this.value != that.value) return false
    return true
}