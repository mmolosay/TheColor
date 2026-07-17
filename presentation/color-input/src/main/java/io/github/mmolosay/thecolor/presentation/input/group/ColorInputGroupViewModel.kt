package io.github.mmolosay.thecolor.presentation.input.group

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.common.viewmodel.Store
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexDataFactory
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvDataFactory
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvViewModel
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbDataFactory
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

/**
 * Handles presentation logic of the 'Color Input Group' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
class ColorInputGroupViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted private val store: Store<ColorInputGroupData>,
    @Assisted mediator: ColorInputMediator,
    @Assisted submitAction: ColorInputSubmitAction,
    hexDataFactory: ColorInputHexDataFactory,
    hexViewModelFactory: ColorInputHexViewModel.Factory,
    rgbDataFactory: ColorInputRgbDataFactory,
    rgbViewModelFactory: ColorInputRgbViewModel.Factory,
    hsvDataFactory: ColorInputHsvDataFactory,
    hsvViewModelFactory: ColorInputHsvViewModel.Factory,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val orderedUpdates = defaultDispatcher.limitedParallelism(1)

    val hexViewModel: ColorInputHexViewModel =
        hexViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
            store = run {
                val data = hexDataFactory.create()
                Store(data)
            },
            mediator = mediator,
            submitAction = submitAction,
        )

    val rgbViewModel: ColorInputRgbViewModel =
        rgbViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
            store = run {
                val data = rgbDataFactory.create()
                Store(data)
            },
            mediator = mediator,
            submitAction = submitAction,
        )

    val hsvViewModel: ColorInputHsvViewModel =
        hsvViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
            store = run {
                val data = hsvDataFactory.create()
                Store(data)
            },
            mediator = mediator,
        )

    val dataFlow: StateFlow<ColorInputGroupData> =
        store.flow.stateIn(coroutineScope, SharingStarted.Eagerly, store.value)

    fun execute(action: ColorInputGroupAction): Job =
        coroutineScope.launch(orderedUpdates) {
            when (action) {
                is ColorInputGroupAction.ChangeInputType -> {
                    changeInputType(action.type)
                }
            }
        }

    private suspend fun changeInputType(type: DomainColorInputType) {
        store.update {
            it.copy(selectedInputType = type)
        }
    }

    override fun dispose() {
        super.dispose()
        hexViewModel.dispose()
        rgbViewModel.dispose()
        hsvViewModel.dispose()
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            store: Store<ColorInputGroupData>,
            mediator: ColorInputMediator,
            submitAction: ColorInputSubmitAction,
        ): ColorInputGroupViewModel
    }
}

class ColorInputGroupDataFactory @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    fun create(): ColorInputGroupData {
        val preferredInputType = userPreferencesRepository.flowOfColorInputType
            .value.getOrElse { error("must be ready") }
        // make list of all input types with the preferred one being first
        val orderedInputTypes = run {
            val allInputTypes = DomainColorInputType.entries
            val allInputTypesWithoutPreferredOne = allInputTypes.filter { it != preferredInputType }
            listOf(preferredInputType) + allInputTypesWithoutPreferredOne
        }
        return ColorInputGroupData(
            selectedInputType = preferredInputType,
            orderedInputTypes = orderedInputTypes,
        )
    }
}