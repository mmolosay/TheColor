package io.github.mmolosay.thecolor.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Storage that holds a [Flow] of [ColorInputEvent]s from a color input View.
 */
@Singleton
class ColorInputEventStore @Inject constructor() : ColorInputEventProvider {

    private val _eventFlow = MutableSharedFlow<ColorInputEvent>(replay = 0)
    override val eventFlow: Flow<ColorInputEvent> = _eventFlow.asSharedFlow()

    suspend infix fun send(event: ColorInputEvent) {
        _eventFlow.emit(event)
    }
}

/** Read-only provider. */
interface ColorInputEventProvider {
    val eventFlow: Flow<ColorInputEvent>
}

/**
 * Event that originates in color input View and is broadcasted to outside.
 */
sealed interface ColorInputEvent {
    /** Submit current color for a further processing outside of color input scope. */
    data object Submit : ColorInputEvent
}