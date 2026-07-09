package io.github.mmolosay.thecolor.presentation.input.group

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.presentation.common.viewmodel.Lens
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.common.viewmodel.Store
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexDataFactory
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexFacadeFactory
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvDataFactory
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvFacadeFactory
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvViewModel
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbDataFactory
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbFacadeFactory
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
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
    hexViewModelFactory: ColorInputHexViewModel.Factory,
    rgbViewModelFactory: ColorInputRgbViewModel.Factory,
    hsvViewModelFactory: ColorInputHsvViewModel.Factory,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val orderedUpdates = defaultDispatcher.limitedParallelism(1)

    private val hexViewModel: ColorInputHexViewModel =
        hexViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
            store = store.focus(
                lens = Lens(
                    get = { s -> s.hex },
                    set = { s, v -> s.copy(hex = v) },
                ),
            ),
            mediator = mediator,
            submitAction = submitAction,
        )
    private val rgbViewModel: ColorInputRgbViewModel =
        rgbViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
            store = store.focus(
                lens = Lens(
                    get = { s -> s.rgb },
                    set = { s, v -> s.copy(rgb = v) },
                ),
            ),
            mediator = mediator,
            submitAction = submitAction,
        )
    private val hsvViewModel: ColorInputHsvViewModel =
        hsvViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
            store = store.focus(
                lens = Lens(
                    get = { s -> s.hsv },
                    set = { s, v -> s.copy(hsv = v) },
                ),
            ),
            mediator = mediator,
        )

    val facadeFactory = ColorInputGroupFacadeFactory(
        hexFacadeFactory = hexViewModel.facadeFactory,
        rgbFacadeFactory = rgbViewModel.facadeFactory,
        hsvFacadeFactory = hsvViewModel.facadeFactory,
        execute = ::execute,
    )

    val dataFlow: StateFlow<ColorInputGroupData> =
        store.flow.stateIn(coroutineScope, SharingStarted.Lazily, store.value)

    fun execute(action: ColorInputGroupAction) {
        coroutineScope.launch(orderedUpdates) {
            when (action) {
                is ColorInputGroupAction.ChangeInputType -> {
                    changeInputType(action.type)
                }
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
    private val colorInputHexDataFactory: ColorInputHexDataFactory,
    private val colorInputRgbDataFactory: ColorInputRgbDataFactory,
    private val colorInputHsvDataFactory: ColorInputHsvDataFactory,
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
            hex = colorInputHexDataFactory.create(),
            rgb = colorInputRgbDataFactory.create(),
            hsv = colorInputHsvDataFactory.create(),
            selectedInputType = preferredInputType,
            orderedInputTypes = orderedInputTypes,
        )
    }
}

class ColorInputGroupFacadeFactory(
    private val hexFacadeFactory: ColorInputHexFacadeFactory,
    private val rgbFacadeFactory: ColorInputRgbFacadeFactory,
    private val hsvFacadeFactory: ColorInputHsvFacadeFactory,
    private val execute: (ColorInputGroupAction) -> Unit,
) {
    fun create(data: ColorInputGroupData): ColorInputGroupFacade =
        ColorInputGroupFacade(
            hex = hexFacadeFactory.create(data.hex),
            rgb = rgbFacadeFactory.create(data.rgb),
            hsv = hsvFacadeFactory.create(data.hsv),
            execute = this.execute,
            selectedInputType = data.selectedInputType,
            orderedInputTypes = data.orderedInputTypes,
        )
}