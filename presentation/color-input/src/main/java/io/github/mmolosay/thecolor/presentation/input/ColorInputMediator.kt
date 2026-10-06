package io.github.mmolosay.thecolor.presentation.input

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator.ColorState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong
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
 *
 * Changes are ordered by when they were made, not by when they are written: every change carries
 * a [ColorState.Revision], and the state always reflects the latest change made.
 * A write of a change that was made before the current state's one is ignored,
 * however late it arrives.
 */
class ColorInputMediator {

    private val mutex = Mutex()
    private val revisionFactory = RevisionFactory()

    private val _colorStateFlow = run {
        val value = ColorState(color = null, source = null, revision = revisionFactory.new())
        MutableStateFlow(value)
    }
    val colorStateFlow: StateFlow<ColorState> = _colorStateFlow.asStateFlow()

    /**
     * Returns a [ColorState.Revision] that is later than every revision returned before.
     * Take it when a change is made (decided).
     * Safe for concurrency.
     */
    fun newRevision(): ColorState.Revision =
        revisionFactory.new()

    /**
     * Executes the given [block] under the mediator's internal [Mutex], providing an [Editor]
     * for safe state updates, and returns the result of the [block].
     * Can be used to "reserve" the mediator for a planned update in the future.
     */
    @OptIn(ExperimentalContracts::class)
    suspend fun <R> withLock(block: suspend (Editor) -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        val owner = Any()
        return mutex.withLock(owner) {
            val editor = Editor(owner)
            block(editor)
        }
    }

    private class RevisionFactory {
        val nextValue = AtomicLong()
        fun new(): ColorState.Revision =
            ColorState.Revision(value = nextValue.getAndIncrement())
    }

    /**
     * State of the color across the 'Color Input' feature.
     *
     * @param color the current color. `null` means that the color is absent or invalid.
     * @param source the place this update of the [ColorState] originates from.
     * @param revision the change that produced this state. Also distinguishes between different
     * [ColorState]s with the same values.
     */
    data class ColorState(
        val color: Color?,
        val source: Source?,
        val revision: Revision,
    ) {
        interface Source

        @JvmInline
        value class Revision(private val value: Long) : Comparable<Revision> {
            override fun compareTo(other: Revision): Int =
                this.value compareTo other.value
        }
    }

    /**
     * Allows mutating [colorStateFlow] in a thread-safe, synchronized context under the lock
     * of the internal [mutex].
     */
    inner class Editor(
        private val lockOwner: Any,
    ) {
        /**
         * Exposes the specified [color] as the [ColorState] from the [colorStateFlow],
         * unless the current state reflects a change made later than [revision].
         *
         * @param color The new [Color] to be set, or `null` if the color should be erased.
         * @param source The type of 'Color Input' that triggered this update, or `null` if the
         * update was triggered programmatically.
         * @param revision The revision taken when this change was decided, see [newRevision].
         * @return `true` if the change was applied; `false` if it was ignored because the [revision] is stale.
         */
        fun set(
            color: Color?,
            source: ColorState.Source? = null,
            revision: ColorState.Revision,
        ): Boolean {
            check(mutex.isLocked) { "Must be called under the mutex's lock" }
            check(mutex.holdsLock(lockOwner)) { "This editor doesn't belong to the current mutex's lock" }
            if (revision > colorState.revision) {
                _colorStateFlow.value = ColorState(color, source, revision)
                return true
            }
            return false
        }
    }
}

val ColorInputMediator.colorState: ColorState
    get() = this.colorStateFlow.value

suspend fun ColorInputMediator.set(
    color: Color?,
    source: ColorState.Source? = null,
    revision: ColorState.Revision,
): Boolean =
    this.withLock { editor ->
        editor.set(color, source, revision)
    }

/**
 * A [ColorState.Source] that is 'Color Input' feature of the specified [type].
 */
data class ColorInputSource(
    val type: DomainColorInputType,
) : ColorState.Source

@Module
@InstallIn(ViewModelComponent::class)
object ColorInputMediatorModule {
    @Provides
    @ViewModelScoped
    fun provideColorInputMediator() = ColorInputMediator()
}