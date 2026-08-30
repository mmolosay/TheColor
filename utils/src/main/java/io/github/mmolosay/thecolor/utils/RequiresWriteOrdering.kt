package io.github.mmolosay.thecolor.utils

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This write does not impose its own call-ordering. " +
            "The caller must serialize it — e.g. by confining calls to a single-threaded context, or by issuing them sequentially within a single transaction. " +
            "Calling it from a context that does not guarantee ordering can let concurrent writes apply out of order (last-write-wins races). " +
            "If your context does not provide ordering, use the safe, ordered API instead.",
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class RequiresWriteOrdering