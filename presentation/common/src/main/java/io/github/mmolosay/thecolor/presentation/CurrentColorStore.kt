package io.github.mmolosay.thecolor.presentation

import io.github.mmolosay.thecolor.domain.model.Color
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mutable storage of current color.
 */
@Singleton
class CurrentColorStore @Inject constructor() : CurrentColorProvider {
    override var color: Color? = null
}

/**
 * Read-only provider of current color.
 */
interface CurrentColorProvider {
    val color: Color?
}

val CurrentColorProvider.requireColor: Color
    get() = requireNotNull(color)