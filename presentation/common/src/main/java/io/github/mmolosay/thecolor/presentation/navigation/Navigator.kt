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
 */
@Singleton
class Navigator @Inject constructor() {

    private val _navEventFlow = MutableSharedFlow<NavEvent>(replay = 0)
    val navEventFlow: Flow<NavEvent> = _navEventFlow.asSharedFlow()

    suspend fun send(navEvent: NavEvent) {
        _navEventFlow.emit(navEvent)
    }
}