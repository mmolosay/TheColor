package io.github.mmolosay.thecolor.presentation

import io.github.mmolosay.thecolor.domain.model.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Storage that holds a [Flow] of [ColorCenterCommand]s for a color center View to handle.
 */
@Singleton
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
    data class FetchData(val color: Color) : ColorCenterCommand
}