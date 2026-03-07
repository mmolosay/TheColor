package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.color.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

/**
 * Acts as a mediator between ViewModels of different 'Color Input' types.
 *
 * The responsibility of this component is to be a single source of truth regarding the color the user
 * currently works with in the 'Color Input' View(s).
 * This helps to synchronize the data between all types of 'Color Input'.
 * This class may also be used to set a specific color to all 'Color Input' types.
 */
class ColorInputMediator @Inject constructor() {

    private val _colorStateFlow = MutableStateFlow(InitialColorState)
    val colorStateFlow: StateFlow<ColorState> = _colorStateFlow.asStateFlow()

    private val mutex = Mutex()
    private var nextId = colorState.id + 1

    /**
     * Executes the given [block] under the mediator's internal [Mutex], providing an [Editor]
     * for safe state updates.
     * Can be used to "reserve" the mediator for a planned update in the future.
     */
    @OptIn(ExperimentalContracts::class)
    suspend fun withLock(block: suspend (Editor) -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        val owner = Any()
        mutex.withLock(owner) {
            val editor = Editor(owner)
            block(editor)
        }
    }

    /**
     * State of the color across the 'Color Input' feature.
     *
     * @param color the current color. `null` means that the color is absent or invalid.
     * @param source the type of the 'Color Input' this [color] originates from.
     * `null` means that this [color] didn't come from any particular type of the 'Color Input' but was set programmatically.
     * @param id a unique ID to distinguish between different [ColorState]s with the same values.
     */
    data class ColorState(
        val color: Color?,
        val source: DomainColorInputType?,
        val id: Int,
    )

    /**
     * Allows mutating [colorStateFlow] in a thread-safe, synchronized context under the lock
     * of the internal [mutex].
     */
    inner class Editor(
        private val lockOwner: Any,
    ) {
        /**
         * Exposes the specified [color] as the [ColorState] from the [colorStateFlow].
         *
         * @param color The new [Color] to be set, or `null` if the color should be erased.
         * @param source The type of 'Color Input' that triggered this update, or `null` if the
         * update was triggered programmatically.
         */
        fun set(color: Color?, source: DomainColorInputType? = null) {
            check(mutex.isLocked) { "Must be called under the mutex's lock" }
            check(mutex.holdsLock(lockOwner)) { "This editor doesn't belong to the current mutex's lock" }
            _colorStateFlow.value = ColorState(color = color, source = source, id = nextId++)
        }
    }

    companion object {
        val InitialColorState = ColorState(color = null, source = null, id = 0)
    }
}

val ColorInputMediator.colorState: ColorInputMediator.ColorState
    get() = this.colorStateFlow.value

suspend fun ColorInputMediator.set(color: Color?, source: DomainColorInputType? = null) =
    this.withLock { editor ->
        editor.set(color = color, source = source)
    }