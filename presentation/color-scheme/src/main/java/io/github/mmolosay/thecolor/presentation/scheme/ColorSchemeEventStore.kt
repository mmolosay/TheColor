package io.github.mmolosay.thecolor.presentation.scheme

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import io.github.mmolosay.thecolor.domain.model.ColorDetails as DomainColorDetails

/**
 * Storage that holds a [Flow] of [ColorSchemeEvent]s from a Color Scheme feature.
 */
class ColorSchemeEventStore @Inject constructor() : ColorSchemeEventProvider {

    private val _eventFlow = MutableSharedFlow<ColorSchemeEvent>(replay = 0)
    override val eventFlow: Flow<ColorSchemeEvent> = _eventFlow.asSharedFlow()

    suspend infix fun send(event: ColorSchemeEvent) {
        _eventFlow.emit(event)
    }
}

/** Read-only provider. */
interface ColorSchemeEventProvider {
    val eventFlow: Flow<ColorSchemeEvent>
}

/** An event that originates in Color Details feature and is broadcast to outside. */
sealed interface ColorSchemeEvent {

    /** A [swatch] has been selected in Color Scheme feature. */
    data class SwatchSelected(
        val swatch: ColorSchemeData.Swatch,
        val swatchColorDetails: DomainColorDetails,
    ) : ColorSchemeEvent
}