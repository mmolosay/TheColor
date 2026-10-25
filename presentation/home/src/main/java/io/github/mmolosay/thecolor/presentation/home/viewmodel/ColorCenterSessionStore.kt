package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterSessionStore.SessionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/* 'internal' for testing, 'private' for HomeViewModel */
internal class ColorCenterSessionStore {

    private val _flowOfSessionState = MutableStateFlow<SessionState>(SessionState.NoSession)
    val flowOfSessionState = _flowOfSessionState.asStateFlow()

    private val updateStateMutex = Mutex()

    suspend fun clear() =
        updateStateMutex.withLock {
            sessionState.cancelIfBuilding()
            _flowOfSessionState.emit(SessionState.NoSession)
        }

    suspend fun startBuilding(seed: Color, job: Job): SessionBuildingScope =
        updateStateMutex.withLock {
            sessionState.cancelIfBuilding()
            val newState = SessionState.BeingBuilt(seed, job)
            _flowOfSessionState.emit(newState)
            return SessionBuildingScopeImpl(origin = newState)
        }

    private fun SessionState.cancelIfBuilding() {
        if (this is SessionState.BeingBuilt) {
            this.job.cancel()
        }
    }

    interface SessionBuildingScope {

        /**
         * Completes the building process of this scope.
         * Emits [SessionState.Ongoing] containing [session] from the [flowOfSessionState].
         *
         * This method is cancellation-cooperative, meaning that if the [SessionState.BeingBuilt.job]
         * is already canceled, then the method will re-throw original [kotlin.coroutines.cancellation.CancellationException].
         *
         * @return `true` if the process was completed successfully, `false` if the [sessionState]
         * has changed already and the scope is "stale".
         */
        suspend fun complete(session: ColorCenterSession): Boolean

        /**
         * Cancels the building process of this scope.
         * Emits [SessionState.NoSession] from the [flowOfSessionState].
         *
         * @return `true` if the process was canceled successfully, `false` if the [sessionState]
         * has changed already and the scope is "stale".
         */
        suspend fun cancel(): Boolean
    }

    private inner class SessionBuildingScopeImpl(
        private val origin: SessionState.BeingBuilt,
    ) : SessionBuildingScope {

        override suspend fun complete(session: ColorCenterSession): Boolean {
            origin.job.ensureActive()
            updateStateMutex.withLock {
                if (sessionState == origin) {
                    _flowOfSessionState.emit(SessionState.Ongoing(session))
                    return true
                }
            }
            return false
        }

        override suspend fun cancel(): Boolean {
            updateStateMutex.withLock {
                if (sessionState == origin) {
                    origin.job.cancel()
                    _flowOfSessionState.emit(SessionState.NoSession)
                    return true
                }
            }
            return false
        }
    }

    sealed interface SessionState {
        data object NoSession : SessionState
        data class BeingBuilt(val seed: Color, val job: Job) : SessionState
        data class Ongoing(val session: ColorCenterSession) : SessionState
    }
}

internal val ColorCenterSessionStore.sessionState: SessionState
    get() = this.flowOfSessionState.value

internal fun SessionState.mustBeOngoing() {
    check(this is SessionState.Ongoing) { "SessionState $this must be Ongoing" }
}

@Suppress("unused") // I personally feel that this piece of logic may return to 'HomeViewModel' in some future
internal fun Color.doesBelongToCurrentSession(
    sessionState: SessionState,
    colorComparator: () -> ColorComparator,
    doesColorBelongToSession: () -> DoesColorBelongToSessionUseCase,
): Boolean {
    val color = this
    return when (sessionState) {
        is SessionState.NoSession ->
            false // no session -> nothing to belong to
        is SessionState.BeingBuilt ->
            with(colorComparator()) { color isSameAs sessionState.seed } // started this session
        is SessionState.Ongoing ->
            with(doesColorBelongToSession()) { color doesBelongTo sessionState.session }
    }
}