package io.github.mmolosay.thecolor.presentation.api

import io.github.mmolosay.thecolor.domain.model.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

/**
 * Storage that holds a [Flow] of [ColorCenterCommand]s for a color center ViewModel to handle.
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
class ColorCenterCommandStore @Inject constructor() : ColorCenterCommandProvider {

    private val _commandFlow = MutableSharedFlow<ColorCenterCommand>(replay = 1)
    override val commandFlow: Flow<ColorCenterCommand> = _commandFlow.asSharedFlow()

    suspend infix fun issue(command: ColorCenterCommand) {
        _commandFlow.emit(command)
    }
}

/** Read-only provider. */
interface ColorCenterCommandProvider {
    val commandFlow: Flow<ColorCenterCommand>
}

sealed interface ColorCenterCommand {

    /** Update a feature (color details, color scheme) with new data. */
    data class FetchData(
        val color: Color,
        val colorRole: ColorRole?,
    ) : ColorCenterCommand
}