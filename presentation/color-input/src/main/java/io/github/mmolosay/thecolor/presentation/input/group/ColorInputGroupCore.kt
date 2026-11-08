package io.github.mmolosay.thecolor.presentation.input.group

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

class ColorInputGroupCore @AssistedInject constructor() {

    fun initialState(): ColorInputGroupState {
        return ColorInputGroupState(
            preferredType = null,
            selectedType = null,
        )
    }

    // region Getters

    fun ColorInputGroupState.isInitialized(): Boolean =
        (this.preferredType != null)

    fun ColorInputGroupState.toViewData(): ColorInputGroupViewData? {
        val preferred = this.preferredType ?: return null
        val ordered = run {
            val others = DomainColorInputType.entries.filter { it != preferred }
            buildList {
                add(preferred)
                addAll(others)
            }
        }
        return ColorInputGroupViewData(
            selectedType = this.selectedType ?: preferred,
            orderedTypes = ordered,
        )
    }

    // endregion

    // region Transitions

    fun ColorInputGroupState.applyPreferredType(
        type: DomainColorInputType,
    ): Change<ColorInputGroupState, Nothing> {
        val state = this.copy(preferredType = type)
        return Change(state)
    }

    fun ColorInputGroupState.select(type: DomainColorInputType): Change<ColorInputGroupState, Nothing> {
        val state = this.copy(selectedType = type)
        return Change(state)
    }

    // endregion

    @AssistedFactory
    fun interface Factory {
        fun create(): ColorInputGroupCore
    }
}