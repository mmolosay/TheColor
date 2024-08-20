package io.github.mmolosay.thecolor.presentation.api

/**
 * Depicts a role of a color in relation to some other color (usually the current one).
 * Consider `null` to be a standalone color that has no counterpart color.
 */
enum class ColorRole {
    Exact,
    Initial,
}