package io.github.mmolosay.thecolor.presentation.details.viewmodel

import io.github.mmolosay.thecolor.domain.color.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import io.github.mmolosay.thecolor.domain.color.ColorDetails as DomainColorDetails

/**
 * Storage that holds a [Flow] of [ColorDetailsCommand]s for a 'Color Details' to handle.
 */
class ColorDetailsCommandStore @Inject constructor() : ColorDetailsCommandProvider {

    private val _commandFlow = MutableSharedFlow<ColorDetailsCommand>(replay = Int.MAX_VALUE)
    override val commandFlow: Flow<ColorDetailsCommand> = _commandFlow.asSharedFlow()

    suspend infix fun issue(command: ColorDetailsCommand) {
        _commandFlow.emit(command)
    }
}

/** Read-only provider. */
interface ColorDetailsCommandProvider {
    val commandFlow: Flow<ColorDetailsCommand>
}

/** A command issued towards 'Color Details' feature to be handled by it. */
sealed interface ColorDetailsCommand {

    /**
     * Sets the specified [color] as the "seed" color for the 'Color Details' feature
     * and fetches the color details for it.
     */
    data class SetSeedColor(
        val color: Color,
    ) : ColorDetailsCommand

    /**
     * Same as the [SetSeedColor], but provides the [details] of the "seed" color to use.
     */
    data class SetSeedDetails(
        val details: DomainColorDetails,
    ) : ColorDetailsCommand

    /**
     * Selects a color with the specified [ColorRole].
     * Requires the "seed" color to be set.
     */
    data class SelectColor(
        val colorRole: ColorRole,
    ) : ColorDetailsCommand
}