package io.github.mmolosay.thecolor.presentation.center

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.presentation.center.ColorCenterData.SideEffect
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeViewModel
import io.github.mmolosay.thecolor.utils.SideEffectIdFactory
import io.github.mmolosay.thecolor.utils.Store
import kotlinx.collections.immutable.minus
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Handles presentation logic of the 'Color Center' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
class ColorCenterViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted private val store: Store<ColorCenterData>,
    @Assisted val colorDetailsViewModel: ColorDetailsViewModel,
    @Assisted val colorSchemeViewModel: ColorSchemeViewModel,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    val dataFlow: StateFlow<ColorCenterData> = store.flow

    private val exclusiveLane = defaultDispatcher.limitedParallelism(1)
    private val seFactory = SideEffectFactory()

    fun execute(action: ColorCenterAction): Job =
        coroutineScope.launch(exclusiveLane) {
            when (action) {
                is ColorCenterAction.ChangePage -> {
                    changePage(action.pageIndex)
                }
                is ColorCenterAction.OnSideEffectProcessed -> {
                    onSideEffectProcessed(action.se)
                }
            }
        }

    private suspend fun changePage(pageIndex: Int) {
        val se = seFactory.changePage(pageIndex)
        store.update {
            it.copy(sideEffects = it.sideEffects.toPersistentList() + se)
        }
    }

    private suspend fun onSideEffectProcessed(se: SideEffect) {
        store.update {
            val newSideEffects = it.sideEffects.toPersistentList() - se
            it.copy(sideEffects = newSideEffects)
        }
    }

    override fun dispose() {
        super.dispose()
        colorDetailsViewModel.dispose()
        colorSchemeViewModel.dispose()
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            store: Store<ColorCenterData>,
            colorDetailsViewModel: ColorDetailsViewModel,
            colorSchemeViewModel: ColorSchemeViewModel,
        ): ColorCenterViewModel
    }
}

private class SideEffectFactory {

    private val idFactory = SideEffectIdFactory()

    fun changePage(pageIndex: Int): SideEffect.ChangePage =
        SideEffect.ChangePage(
            id = idFactory.get(),
            pageIndex = pageIndex,
        )
}