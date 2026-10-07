package io.github.mmolosay.thecolor.presentation.input.group

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.filterReady
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexHandle
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexStateFactory
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvHandle
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvStateFactory
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvViewModel
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorState
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbHandle
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbStateFactory
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
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
    @Assisted initialData: ColorInputGroupData,
    @Assisted mediator: ColorInputMediator,
    @Assisted submitAction: ColorInputSubmitAction,
    hexViewModelFactory: ColorInputHexViewModel.Factory,
    rgbViewModelFactory: ColorInputRgbViewModel.Factory,
    hsvViewModelFactory: ColorInputHsvViewModel.Factory,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val exclusiveLane = defaultDispatcher.limitedParallelism(1)

    private val _dataFlow = MutableStateFlow(initialData)
    val dataFlow: StateFlow<ColorInputGroupData> = _dataFlow.asStateFlow()

    private val hexViewModel: ColorInputHexViewModel =
        hexViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
            mediator = mediator,
            submitAction = submitAction,
        )
    val hexHandle = ColorInputHexHandle(hexViewModel)

    private val rgbViewModel: ColorInputRgbViewModel =
        rgbViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
            mediator = mediator,
            submitAction = submitAction,
        )
    val rgbHandle = ColorInputRgbHandle(rgbViewModel)

    private val hsvViewModel: ColorInputHsvViewModel =
        hsvViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
            mediator = mediator,
        )
    val hsvHandle = ColorInputHsvHandle(hsvViewModel)

    fun execute(action: ColorInputGroupAction): Job =
        coroutineScope.launch(exclusiveLane) {
            when (action) {
                is ColorInputGroupAction.ChangeInputType -> {
                    changeInputType(action.type)
                }
            }
        }

    private fun changeInputType(type: DomainColorInputType) {
        _dataFlow.update {
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
            initialData: ColorInputGroupData,
            mediator: ColorInputMediator,
            submitAction: ColorInputSubmitAction,
        ): ColorInputGroupViewModel
    }
}

class ColorInputGroupDataFactory @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend fun create(): ColorInputGroupData {
        val preferredInputType = userPreferencesRepository.flowOfColorInputType
            .filterReady().first()
            .getOrElse { DefaultUserPreferences.PreferredColorInputType }
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

class ColorInputGroupStateFactory @Inject constructor(
    private val hexFactory: ColorInputHexStateFactory,
    private val rgbFactory: ColorInputRgbStateFactory,
    private val hsvFactory: ColorInputHsvStateFactory,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend fun create(color: Color?): ColorInputGroupState {
        val preferredInputType = userPreferencesRepository.flowOfColorInputType
            .filterReady().first()
            .getOrElse { DefaultUserPreferences.PreferredColorInputType }
        // make list of all input types with the preferred one being first
        val orderedInputTypes = run {
            val allInputTypes = DomainColorInputType.entries
            val allInputTypesWithoutPreferredOne = allInputTypes.filter { it != preferredInputType }
            listOf(preferredInputType) + allInputTypesWithoutPreferredOne
        }
        return ColorInputGroupState(
            selectedInputType = preferredInputType,
            orderedInputTypes = orderedInputTypes,
            colorState = ColorState(
                color = color,
                source = null,
                revision = ColorState.Revision(0),
            ),
            hex = hexFactory.create(color),
            rgb = rgbFactory.create(color),
            hsv = hsvFactory.create(color),
        )
    }
}