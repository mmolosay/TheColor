package io.github.mmolosay.thecolor.presentation.scheme

import io.github.mmolosay.thecolor.domain.model.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

/**
 * Storage that holds a [Flow] of [ColorSchemeCommand]s for a Color Scheme to handle.
 */
class ColorSchemeCommandStore @Inject constructor() : ColorSchemeCommandProvider {

    private val _commandFlow = MutableSharedFlow<ColorSchemeCommand>(replay = 1)
    override val commandFlow: Flow<ColorSchemeCommand> = _commandFlow.asSharedFlow()

    suspend infix fun issue(command: ColorSchemeCommand) {
        _commandFlow.emit(command)
    }
}

/** Read-only provider. */
interface ColorSchemeCommandProvider {
    val commandFlow: Flow<ColorSchemeCommand>
}

/** A command issued towards Color Scheme feature to be handled by it. */
sealed interface ColorSchemeCommand {

    /** Request to obtain data using specified parameters. */
    data class FetchData(
        val color: Color,
    ) : ColorSchemeCommand
}