package io.github.mmolosay.thecolor.presentation.details

import io.github.mmolosay.thecolor.domain.model.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import io.github.mmolosay.thecolor.domain.model.ColorDetails as DomainColorDetails

/**
 * Storage that holds a [Flow] of [ColorDetailsCommand]s for a Color Details to handle.
 */
class ColorDetailsCommandStore @Inject constructor() : ColorDetailsCommandProvider {

    private val _commandFlow = MutableSharedFlow<ColorDetailsCommand>(replay = 1)
    override val commandFlow: Flow<ColorDetailsCommand> = _commandFlow.asSharedFlow()

    suspend infix fun issue(command: ColorDetailsCommand) {
        _commandFlow.emit(command)
    }
}

/** Read-only provider. */
interface ColorDetailsCommandProvider {
    val commandFlow: Flow<ColorDetailsCommand>
}

/** A command issued towards Color Details feature to be handled by it. */
sealed interface ColorDetailsCommand {

    /** Request to obtain data using specified parameters. */
    data class FetchData(
        val color: Color,
        val colorRole: ColorRole?,
    ) : ColorDetailsCommand

    /** Update data with specified [domainDetails]. */
    data class SetColorDetails(
        val domainDetails: DomainColorDetails,
    ) : ColorDetailsCommand
}