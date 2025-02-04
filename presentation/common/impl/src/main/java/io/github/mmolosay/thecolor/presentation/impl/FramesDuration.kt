package io.github.mmolosay.thecolor.presentation.impl

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Calculates [Duration] of specified number of UI frames.
 * The resulting duration is a __rough approximation__ that's NOT based on executing device specs.
 */
val Int.framesDuration: Duration
    get() = FrameDurationAt60hz * this

val FrameDurationAt60hz = 1.seconds / 60