package io.github.mmolosay.thecolor.presentation.details

import io.github.mmolosay.thecolor.domain.model.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

/**
 * Storage that holds a [Flow] of [ColorDetailsEvent]s from a Color Details feature.
 */
class ColorDetailsEventStore @Inject constructor() : ColorDetailsEventProvider {

    private val _eventFlow = MutableSharedFlow<ColorDetailsEvent>(replay = 0)
    override val eventFlow: Flow<ColorDetailsEvent> = _eventFlow.asSharedFlow()

    suspend infix fun send(event: ColorDetailsEvent) {
        _eventFlow.emit(event)
    }
}

/** Read-only provider. */
interface ColorDetailsEventProvider {
    val eventFlow: Flow<ColorDetailsEvent>
}

/** An event that originates in Color Details feature and is broadcast to outside. */
sealed interface ColorDetailsEvent {

    /** A [color] has been selected in Color Details feature. */
    data class ColorSelected(
        val color: Color,
        val colorRole: ColorRole,
    ) : ColorDetailsEvent
}