package io.github.mmolosay.thecolor.presentation

import io.github.mmolosay.thecolor.domain.model.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

/**
 * Storage that holds a [Flow] of [ColorCenterEvent]s from a color center View and its sub-Views:
 * color details View and color scheme View.
 */
class ColorCenterEventStore @Inject constructor() : ColorCenterEventProvider {

    private val _eventFlow = MutableSharedFlow<ColorCenterEvent>(replay = 0)
    override val eventFlow: Flow<ColorCenterEvent> = _eventFlow.asSharedFlow()

    suspend infix fun send(event: ColorCenterEvent) {
        _eventFlow.emit(event)
    }
}

/** Read-only provider. */
interface ColorCenterEventProvider {
    val eventFlow: Flow<ColorCenterEvent>
}

/**
 * Event that originates in color center View (or its sub-View) and is broadcasted to outside.
 */
sealed interface ColorCenterEvent {

    /**
     * A [color] has been selected in color details View.
     */
    data class ColorSelected(
        val color: Color,
        val colorRole: ColorRole,
    ) : ColorCenterEvent
}