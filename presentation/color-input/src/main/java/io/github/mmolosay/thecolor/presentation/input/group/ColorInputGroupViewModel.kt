package io.github.mmolosay.thecolor.presentation.input.group

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.filterReady
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.input.ColorInputMapper
import io.github.mmolosay.thecolor.presentation.input.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexHandle
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexStateFactory
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.presentation.input.hex.color
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvHandle
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvState
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvStateFactory
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvViewModel
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorState
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbHandle
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbStateFactory
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbViewModel
import io.github.mmolosay.thecolor.presentation.input.rgb.color
import io.github.mmolosay.thecolor.utils.Atom
import io.github.mmolosay.thecolor.utils.Lens
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupStateLenses as Lenses

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
    @Assisted initialState: ColorInputGroupState,
    @Assisted submitAction: ColorInputSubmitAction,
    hexViewModelFactory: ColorInputHexViewModel.Factory,
    rgbViewModelFactory: ColorInputRgbViewModel.Factory,
    hsvViewModelFactory: ColorInputHsvViewModel.Factory,
    private val colorConverter: ColorConverter,
    private val colorInputMapper: ColorInputMapper,
    private val colorInputValidator: ColorInputValidator,
) : SimpleViewModel(coroutineScope) {

    private val stateFlow = MutableStateFlow(initialState)
    val dataFlow: StateFlow<ColorInputGroupData> = stateFlow.mapState { it.toData() }
    val colorStateFlow: StateFlow<ColorState> = stateFlow.mapState { it.colorState }

    private val hexViewModel: ColorInputHexViewModel =
        hexViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
            atom = InputStateAtom(
                type = DomainColorInputType.Hex,
                lens = Lenses.hex,
                getColor = { context(colorInputValidator) { it.color() } },
            ),
            submitAction = submitAction,
        )
    val hexHandle = ColorInputHexHandle(hexViewModel)

    private val rgbViewModel: ColorInputRgbViewModel =
        rgbViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
            atom = InputStateAtom(
                type = DomainColorInputType.Rgb,
                lens = Lenses.rgb,
                getColor = { context(colorInputValidator) { it.color() } },
            ),
            submitAction = submitAction,
        )
    val rgbHandle = ColorInputRgbHandle(rgbViewModel)

    private val hsvViewModel: ColorInputHsvViewModel =
        hsvViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
            atom = InputStateAtom(
                type = DomainColorInputType.Hsv,
                lens = Lenses.hsv,
                getColor = ColorInputHsvState::color,
            )
        )
    val hsvHandle = ColorInputHsvHandle(hsvViewModel)

    fun execute(action: ColorInputGroupAction): Job? =
        when (action) {
            is ColorInputGroupAction.ChangeInputType -> {
                changeInputType(action.type)
                null
            }
        }

    private fun changeInputType(type: DomainColorInputType) {
        stateFlow.update {
            it.copy(selectedInputType = type)
        }
    }

    fun setColor(color: Color?) {
        context(colorConverter, colorInputMapper) {
            stateFlow.update {
                it.withColor(color, source = null)
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
            initialState: ColorInputGroupState,
            submitAction: ColorInputSubmitAction,
        ): ColorInputGroupViewModel
    }

    private inner class InputStateAtom<T>(
        private val type: DomainColorInputType,
        private val lens: Lens<ColorInputGroupState, T>,
        private val getColor: (T) -> Color?,
    ) : Atom<T> {

        override val value: T
            get() = lens.get(stateFlow.value)

        override fun update(transform: (T) -> T) {
            stateFlow.update { current ->
                val old = lens.get(current)
                val new = transform(old)
                val newColor = getColor(new)
                val base = when (newColor) {
                    getColor(old) -> current // this input still stands for the same color
                    else -> context(colorConverter, colorInputMapper) {
                        current.withColor(newColor, type)
                    }
                }
                lens.set(base, new)
            }
        }
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