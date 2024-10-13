package io.github.mmolosay.thecolor.presentation.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

/**
 * A `MVVM` `ViewModel` in its most classic, platform-agnostic understanding.
 *
 * This component has nothing to do with Google's Android-aware implementation of `ViewModel`.
 * Consider derivatives of this component as just classes with some presentational logic in them.
 *
 * Every derivative should be created and used by a `ViewModel` that DOES derive from Google's
 * Android-aware implementation.
 *
 * @param coroutineScope a scope to be used inside this `ViewModel`. Cancelled in [dispose].
 * If it was created as a child scope of some other scope, canceled when the parent scope is.
 */
abstract class SimpleViewModel(
    protected open val coroutineScope: CoroutineScope,
) {

    open fun dispose() {
        coroutineScope.cancel()
    }
}