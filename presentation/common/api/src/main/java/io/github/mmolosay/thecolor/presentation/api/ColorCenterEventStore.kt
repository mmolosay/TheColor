package io.github.mmolosay.thecolor.presentation.api

import io.github.mmolosay.thecolor.domain.model.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

/**
 * Storage that holds a [Flow] of [ColorCenterEvent]s from a color center View and its sub-Views:
 * color details View and color scheme View.
 */
/*
 * One may think that this file belongs to Color Center feature, and thus should be located at
 * ':presentation:color-center:api' module.
 * But it is not true. Consider following facts:
 * 1. Components in this file are used by "child" features of Color Center feature, which are
 * Color Details and Color Scheme.
 * 2. Color Center module depends on its "child" modules.
 *
 * Thus, by putting these classes in Color Center, it will create a circular dependency, which
 * must be avoided.
 *
 * Ideally, these classes should be put in a module that is "common" for "child" features of
 * Color Center. However, it's unnecessary to spawn such atomic modules for just a file or two.
 * Instead, I've put them in ':presentation:common:api" module, which is just one level higher
 * than this ideal module it was theorised about prior.
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