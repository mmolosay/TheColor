package io.github.mmolosay.thecolor.presentation

import io.github.mmolosay.thecolor.domain.model.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Storage that holds a [Flow] of [Command]s for a color center View to handle.
 */
@Singleton
class ColorCenterCommandStore @Inject constructor() : ColorCenterCommandProvider {

    private val _commandFlow = MutableSharedFlow<Command>(replay = 1)
    override val commandFlow: Flow<Command> = _commandFlow.asSharedFlow()

    suspend fun updateWith(command: Command) {
        _commandFlow.emit(command)
    }
}

/** Read-only provider. */
interface ColorCenterCommandProvider {
    val commandFlow: Flow<Command>
}

sealed interface Command {
    data class FetchData(val color: Color) : Command
}