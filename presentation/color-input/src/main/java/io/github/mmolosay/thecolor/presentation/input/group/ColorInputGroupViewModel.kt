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
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexFacade
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvDataFactory
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvFacade
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvViewModel
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbDataFactory
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbFacade
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

    val dataFlow: StateFlow<ColorInputGroupData> =
        store.flow.stateIn(coroutineScope, SharingStarted.Lazily, store.value)

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

    fun facade(data: ColorInputGroupData): ColorInputGroupFacade =
        ColorInputGroupFacadeImpl(
            data = data,
            viewModel = this,
            hex = hexViewModel.facade(data.hex),
            rgb = rgbViewModel.facade(data.rgb),
            hsv = hsvViewModel.facade(data.hsv),
        )

    fun changeInputType(type: DomainColorInputType) {
        coroutineScope.launch(orderedUpdates) {
            store.update {
                it.copy(selectedInputType = type)
            }
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

private class ColorInputGroupFacadeImpl(
    private val data: ColorInputGroupData,
    private val viewModel: ColorInputGroupViewModel,
    override val hex: ColorInputHexFacade,
    override val rgb: ColorInputRgbFacade,
    override val hsv: ColorInputHsvFacade,
) : ColorInputGroupFacade {

    override val orderedInputTypes = data.orderedInputTypes
    override val selectedInputType = data.selectedInputType
    override fun changeInputType(type: DomainColorInputType) =
        viewModel.changeInputType(type)

    override fun equals(other: Any?): Boolean =
        other is ColorInputGroupFacadeImpl && this.data == other.data && this.viewModel === other.viewModel

    override fun hashCode(): Int {
        var result = data.hashCode()
        result = 31 * result + System.identityHashCode(viewModel)
        return result
    }
}