package io.github.mmolosay.thecolor.presentation.home.input

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.presentation.color.ColorPrototype
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputMediator.Command
import io.github.mmolosay.thecolor.presentation.mapper.toDomainOrNull
import io.github.mmolosay.thecolor.presentation.mapper.toPresentation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Acts as mediator between ViewModels of different color input Views.
 * The responsibility of this component is to synchronize data between different color input Views.
 *
 * Once one View [issue]s a [Command], all other Views get the same command.
 * This way if user was using one specific View, after switching to other View they will see
 * the UI with the same data (color) they have left on in previous View.
 */
@Singleton
class ColorInputMediator @Inject constructor(
    private val colorConverter: ColorConverter,
) {

    private val commandFlow =
        MutableStateFlow<Command<Color.Abstract>>(Command.Clear)
    private lateinit var lastUsedInputType: InputType

    val hexCommandFlow: Flow<Command<ColorPrototype.Hex>> =
        commandFlow.mapColorType(inputType = InputType.Hex) {
            with(colorConverter) { it.toHex() }.toPresentation()
        }

    val rgbCommandFlow: Flow<Command<ColorPrototype.Rgb>> =
        commandFlow.mapColorType(inputType = InputType.Rgb) {
            with(colorConverter) { it.toRgb() }.toPresentation()
        }

    fun <C : ColorPrototype> issue(command: Command<C>) {
        when (command) {
            is Command.Clear -> command // just pass it forward
            is Command.Populate -> {
                val abstract = command.color.toAbstract() ?: return // ignore unfinished colors
                lastUsedInputType = command.color.toInputType()
                Command.Populate(abstract)
            }
        }.also {
            commandFlow.value = it
        }
    }

    private fun <C> Flow<Command<Color.Abstract>>.mapColorType(
        inputType: InputType,
        transform: (Color.Abstract) -> C,
    ) = transform { command ->
        when (command) {
            is Command.Clear -> command
            is Command.Populate -> {
                if (lastUsedInputType == inputType) return@transform // prevent user input interrupting
                val color = transform(command.color)
                Command.Populate(color)
            }
        }.also {
            emit(it)
        }
    }

    // region ColorPrototype.toAbstract()

    private fun ColorPrototype.toAbstract(): Color.Abstract? =
        when (this) {
            is ColorPrototype.Hex -> toAbstract()
            is ColorPrototype.Rgb -> toAbstract()
        }

    private fun ColorPrototype.Hex.toAbstract(): Color.Abstract? {
        val color: Color.Hex = this.toDomainOrNull() ?: return null // ignore unfinished colors
        return with(colorConverter) { color.toAbstract() }
    }

    private fun ColorPrototype.Rgb.toAbstract(): Color.Abstract? {
        val color: Color.Rgb = this.toDomainOrNull() ?: return null // ignore unfinished colors
        return with(colorConverter) { color.toAbstract() }
    }

    // endregion

    private fun ColorPrototype.toInputType() =
        when (this) {
            is ColorPrototype.Hex -> InputType.Hex
            is ColorPrototype.Rgb -> InputType.Rgb
        }

    /** A command to be executed by receiving ViewModel towards its View. */
    sealed interface Command<out C> {

        /** Clears all user input. */
        data object Clear : Command<Nothing>

        /** Populates UI with specified [color] data. */
        data class Populate<C>(val color: C) : Command<C>
    }

    private enum class InputType {
        Hex,
        Rgb,
    }
}