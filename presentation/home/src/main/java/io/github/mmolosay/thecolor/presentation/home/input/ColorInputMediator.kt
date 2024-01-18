package io.github.mmolosay.thecolor.presentation.home.input

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.presentation.color.ColorPrototype
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputMediator.State
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
 * Once one View [send]s a [State], all other Views get the same state through flows.
 * This way if user was using one specific View, after switching to other View they will see
 * the UI with the same data (color) they have left on in previous View.
 */
@Singleton
class ColorInputMediator @Inject constructor(
    private val colorConverter: ColorConverter,
) {

    private val stateFlow = MutableStateFlow<State<Color.Abstract>>(State.Empty)
    private lateinit var lastUsedInputType: InputType

    val hexStateFlow: Flow<State<ColorPrototype.Hex>> =
        stateFlow.transform { state ->
            state.map {
                if (lastUsedInputType == InputType.Hex) return@transform // prevent user input interrupting
                with(colorConverter) { it.toHex() }.toPresentation()
            }.also {
                emit(it)
            }
        }

    val rgbStateFlow: Flow<State<ColorPrototype.Rgb>> =
        stateFlow.transform { state ->
            state.map {
                if (lastUsedInputType == InputType.Rgb) return@transform // prevent user input interrupting
                with(colorConverter) { it.toRgb() }.toPresentation()
            }.also {
                emit(it)
            }
        }

    fun <C : ColorPrototype> send(state: State<C>) {
        stateFlow.value = state.map { color ->
            val result = color.toAbstract() ?: return // ignore unfinished colors
            lastUsedInputType = color.toInputType()
            result
        }
    }

    private inline fun <T, R> State<T>.map(
        transformation: (T) -> R,
    ): State<R> =
        when (this) {
            is State.Empty -> this
            is State.Populated -> {
                val newColor = transformation(color)
                State.Populated(newColor)
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

    /** A state of View in regard of user input. */
    sealed interface State<out C> {

        /** Clears all user input. */
        data object Empty : State<Nothing>

        /** Populates UI with specified [color] data. */
        data class Populated<C>(val color: C) : State<C>
    }

    private enum class InputType {
        Hex,
        Rgb,
    }
}