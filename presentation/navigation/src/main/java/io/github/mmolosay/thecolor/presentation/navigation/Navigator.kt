package io.github.mmolosay.thecolor.presentation.navigation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Platform-agnostic component that exposes [Flow] of [NavEvent]s.
 *
 * Any component that can handle these [NavEvent]s should collect [navEventFlow]
 * and perform according navigation.
 *
 * P.S.: I'm not a fan of name `"Navigator"`. This component is more like `"NavEventsStore"`.
 * However, this name if very concise and easy to spot.
 */
@Singleton
class Navigator @Inject constructor() {

    private val _navEventFlow = MutableSharedFlow<NavEvent>(replay = 0)
    val navEventFlow: Flow<NavEvent> = _navEventFlow.asSharedFlow()

    suspend fun send(navEvent: NavEvent) {
        _navEventFlow.emit(navEvent)
    }
}