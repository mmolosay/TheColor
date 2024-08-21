package io.github.mmolosay.thecolor.presentation.input.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

/**
 * Storage that holds a [Flow] of [ColorInputEvent]s from a Color Input feature.
 */
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
 * Event that originates in Color Input feature and is broadcast to outside.
 */
sealed interface ColorInputEvent {

    /**
     * Submit current color for a further processing outside of Color Input scope.
     * Component that intercepts and processes this event should call [onConsumed] afterwards.
     * */
    data class Submit(
        val colorInput: ColorInput,
        val colorInputState: ColorInputState,
        val onConsumed: OnConsumedAction,
    ) : ColorInputEvent {

        fun interface OnConsumedAction {
            /**
             * @param wasAccepted whether the submitted [ColorInput] was accepted and user will
             * pause their interaction with Color Input for some time.
             */
            operator fun invoke(wasAccepted: Boolean)
        }
    }
}