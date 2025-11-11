package io.github.mmolosay.thecolor.utils

/**
 * Marks APIs that are intended for temporary debugging or development use only.
 *
 * Triggers a compile-time warning when referenced, preventing accidental use in production.
 * You may also want to add `@Suppress("unused")` to the API's declaration to prevent IDE from complaining.
 */
@RequiresOptIn(
    message = "This API is intended for temporary debugging only and should not be used in production code.",
    level = RequiresOptIn.Level.WARNING,
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
annotation class DebugOnly