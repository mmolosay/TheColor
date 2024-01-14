@file:Suppress("unused")

package io.github.mmolosay.presentation.util.ext

import androidx.lifecycle.Lifecycle

/**
 * @see [Lifecycle.State.DESTROYED]
 */
val Lifecycle.State.isDestroyed: Boolean
    get() = (this == Lifecycle.State.DESTROYED)

/**
 * @see [Lifecycle.State.INITIALIZED]
 */
val Lifecycle.State.isInitialized: Boolean
    get() = (this == Lifecycle.State.INITIALIZED)

/**
 * @see [Lifecycle.State.CREATED]
 */
val Lifecycle.State.isCreated: Boolean
    get() = (this == Lifecycle.State.CREATED)

/**
 * @see [Lifecycle.State.STARTED]
 */
val Lifecycle.State.isStarted: Boolean
    get() = (this == Lifecycle.State.STARTED)

/**
 * @see [Lifecycle.State.RESUMED]
 */
val Lifecycle.State.isResumed: Boolean
    get() = (this == Lifecycle.State.RESUMED)