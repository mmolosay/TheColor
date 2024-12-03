package io.github.mmolosay.thecolor.presentation.api.nav.bar

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import io.github.mmolosay.thecolor.presentation.api.nav.bar.NavBarAppearance as Appearance

fun RootNavBarAppearanceController(): NavBarAppearanceController {
    val idFactory = IdFactory()
    val id = idFactory.get()
    return NavBarAppearanceController(
        tag = "root",
        id = id,
        idFactory = idFactory,
        listener = NoopStackChangeListener,
    )
}

/**
 * Controller of navigation bar's appearance.
 *
 * It supports "layering" appearances, so that if the latest appearance is cancelled (peeled),
 * then the one that was before it will be used (as when going back to previous screen).
 *
 * Each screen should be given its own controller using [branch] method.
 */
class NavBarAppearanceController internal constructor(
    @Suppress("unused") private val tag: Any, // simplifies debugging
    private val id: Id,
    private val idFactory: IdFactory,
    private val listener: StackChangeListener,
) {
    private val ownStack = mutableListOf<Appearance.WithTag>()

    private val children = mutableListOf<NavBarAppearanceController>()
    private val mergedStacks =
        mutableListOf<AppearanceWithSource>() // own stack and stacks of descendants

    private val _appearanceFlow = MutableStateFlow<Appearance.WithTag?>(null)

    /**
     * Holds the appearance that was push latest to this controller or any of its descendants.
     * Updated when the latest pushed appearance changes (e.g. a new one is pushed or the top one is removed).
     */
    val appearanceFlow = _appearanceFlow.asStateFlow()

    /**
     * Adds an [appearance] to the top of the controller's stack.
     */
    fun push(appearance: Appearance.WithTag) {
        ownStack += appearance
        modifyMergedStacks {
            mergedStacks += MergedStacksEntry(appearance)
        }
        listener.onPushed(appearance, sourceId = this.id)
    }

    /**
     * Removes an appearance from the top of the controller's stack.
     * Does nothing if there's not a single appearance in the stack.
     */
    fun peel() {
        val removed = ownStack.removeLastOrNull() ?: return
        modifyMergedStacks {
            val entry = MergedStacksEntry(removed)
            mergedStacks.remove(entry)
        }
        listener.onRemoved(removed, sourceId = this.id)
    }

    /**
     * Removes all appearances from the controller's stack.
     * Also clears stacks of all descendant controllers that were created from this controller using [branch].
     */
    fun clear() {
        children.forEach { child ->
            child.clear()
        }
        ownStack.clear()
        modifyMergedStacks {
            mergedStacks.clear()
        }
        listener.onCleared(sourceId = this.id)
    }

    /**
     * Creates a new [NavBarAppearanceController].
     *
     * Returned controller can be thought of as a separate group allocated exclusively for some component.
     * This way, all appearances pushed by this component are grouped together in a single controller.
     *
     * Returned controller is treated as a child of a controller this method was called on.
     * Modifying the appearance stack of a child controller will be hoisted to its ancestors
     * to the very root controller of the hierarchy.
     *
     * Clearing the appearance stack of a parent controller clears stacks of all its descendants.
     *
     * @param tag a qualifier to set to a newly created controller. Simplifies debugging.
     */
    fun branch(tag: Any): NavBarAppearanceController {
        val newChildId = idFactory.get()
        val newChild = NavBarAppearanceController(
            tag = tag,
            id = newChildId,
            idFactory = idFactory,
            listener = StackChangeListenerImpl(),
        )
        children += newChild
        return newChild
    }

    private inline fun modifyMergedStacks(block: () -> Unit) {
        block()
        val top = mergedStacks.lastOrNull()
        _appearanceFlow.value = top?.appearance
    }

    private fun MergedStacksEntry(appearance: Appearance.WithTag) =
        AppearanceWithSource(appearance = appearance, sourceId = this.id)

    private inner class StackChangeListenerImpl : StackChangeListener {

        override fun onPushed(appearance: Appearance.WithTag, sourceId: Id) {
            modifyMergedStacks {
                mergedStacks += AppearanceWithSource(appearance, sourceId)
            }
            listener.onPushed(appearance, sourceId)
        }

        override fun onRemoved(appearance: Appearance.WithTag, sourceId: Id) {
            modifyMergedStacks {
                val entry = AppearanceWithSource(appearance, sourceId)
                val index =
                    mergedStacks.indexOfLast { it == entry } // -1 shouldn't be possible here
                mergedStacks.removeAt(index)
            }
            listener.onRemoved(appearance, sourceId)
        }

        override fun onCleared(sourceId: Id) {
            modifyMergedStacks {
                mergedStacks.removeAll { it.sourceId == sourceId }
            }
            listener.onCleared(sourceId)
        }
    }
}

/**
 * Adds an [appearance] with `null` tag to the top of the stack.
 */
fun NavBarAppearanceController.push(appearance: Appearance) {
    val tagged = appearance withTag null
    this.push(tagged)
}

/**
 * A unique identifier for a [NavBarAppearanceController].
 * Used to link an [Appearance] to the controller it originates from.
 */
@JvmInline
internal value class Id(val int: Int)

internal class IdFactory {
    private var nextInt: Int = 0
    fun get() = Id(int = nextInt++)
}

private data class AppearanceWithSource(
    val appearance: Appearance.WithTag,
    val sourceId: Id,
)

internal interface StackChangeListener {
    fun onPushed(appearance: Appearance.WithTag, sourceId: Id)
    fun onRemoved(appearance: Appearance.WithTag, sourceId: Id)
    fun onCleared(sourceId: Id)
}

private object NoopStackChangeListener : StackChangeListener {
    override fun onPushed(appearance: Appearance.WithTag, sourceId: Id) {}
    override fun onRemoved(appearance: Appearance.WithTag, sourceId: Id) {}
    override fun onCleared(sourceId: Id) {}
}