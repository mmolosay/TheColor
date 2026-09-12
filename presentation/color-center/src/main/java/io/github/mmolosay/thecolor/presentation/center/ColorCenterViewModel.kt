package io.github.mmolosay.thecolor.presentation.center

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.presentation.center.ColorCenterData.SideEffect
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.utils.SideEffectIdFactory
import kotlinx.collections.immutable.minus
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    @Assisted private val _dataFlow: MutableStateFlow<ColorCenterData>,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    val dataFlow: StateFlow<ColorCenterData> = _dataFlow.asStateFlow()

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

    private fun changePage(pageIndex: Int) {
        val se = seFactory.changePage(pageIndex)
        _dataFlow.update {
            it.copy(sideEffects = it.sideEffects.toPersistentList() + se)
        }
    }

    private fun onSideEffectProcessed(se: SideEffect) {
        _dataFlow.update {
            val newSideEffects = it.sideEffects.toPersistentList() - se
            it.copy(sideEffects = newSideEffects)
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            dataFlow: MutableStateFlow<ColorCenterData>,
        ): ColorCenterViewModel
    }
}

class ColorCenterDataFactory @Inject constructor() {
    fun create(): ColorCenterData =
        ColorCenterData(
            sideEffects = persistentListOf(),
        )
}

private class SideEffectFactory {

    private val idFactory = SideEffectIdFactory()

    fun changePage(pageIndex: Int): SideEffect.ChangePage =
        SideEffect.ChangePage(
            id = idFactory.get(),
            pageIndex = pageIndex,
        )
}