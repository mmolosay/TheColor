package io.github.mmolosay.thecolor.presentation.input.rgb

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.filterReady
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.UiDataUpdateDispatcher
import io.github.mmolosay.thecolor.presentation.common.viewmodel.CompositionNode
import io.github.mmolosay.thecolor.presentation.common.viewmodel.CompositionScope
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.input.ColorInputMapper
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.model.getColorOrNull
import io.github.mmolosay.thecolor.presentation.input.set
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldViewModel
import io.github.mmolosay.thecolor.presentation.input.textfield.data
import io.github.mmolosay.thecolor.presentation.input.textfield.updateText
import io.github.mmolosay.thecolor.utils.ActionWithResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

/**
 * Handles presentation logic of the 'RGB Color Input' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
class ColorInputRgbViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted private val compositionScope: CompositionScope,
    @Assisted private val mediator: ColorInputMediator,
    @Assisted private val submitAction: ColorInputSubmitAction,
    private val textFieldViewModelFactory: TextFieldViewModel.Factory,
    private val colorInputValidator: ColorInputValidator,
    private val colorInputMapper: ColorInputMapper,
    private val colorConverter: ColorConverter,
    private val userPreferencesRepository: UserPreferencesRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @UiDataUpdateDispatcher private val uiDataUpdateDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val rTextFieldVm = createTextFieldViewModel()
    private val gTextFieldVm = createTextFieldViewModel()
    private val bTextFieldVm = createTextFieldViewModel()

    private val compositionNode = compositionScope.node(
        initialValue = run {
            ColorInputRgbData(
                rTextField = rTextFieldVm.data,
                gTextField = gTextFieldVm.data,
                bTextField = bTextFieldVm.data,
                submitInput = ActionWithResult(
                    result = null,
                    action = ::submitInput,
                ),
                isSmartBackspaceEnabled = userPreferencesRepository.flowOfSmartBackspace
                    .value.getOrElse { DefaultUserPreferences.SmartBackspace }
                    .enabled,
            )
        },
        recompute = { current ->
            current.copy(
                rTextField = rTextFieldVm.data,
                gTextField = gTextFieldVm.data,
                bTextField = bTextFieldVm.data,
            )
        },
        children = listOf(
            rTextFieldVm.compositionNodeId,
            gTextFieldVm.compositionNodeId,
            bTextFieldVm.compositionNodeId
        ),
    )
    val compositionNodeId: CompositionNode.Id
        get() = compositionNode.id
    val dataFlow: StateFlow<ColorInputRgbData>
        get() = compositionNode.dataFlow

    init {
        collectMediatorUpdates()
        collectTextFieldsData()
        collectSmartBackspacePreference()
    }

    private fun collectMediatorUpdates() {
        coroutineScope.launch(uiDataUpdateDispatcher) {
            mediator.colorStateFlow.collect { (color, source) ->
                // don't update text fields to avoid update loop if the color was set from this 'Color Input' type
                if (source == DomainColorInputType.Rgb) return@collect
                val colorInput = if (color != null) {
                    val hexColor = with(colorConverter) { color.toRgb() }
                    with(colorInputMapper) { hexColor.toColorInput() }
                } else {
                    EmptyColorInput
                }
                rTextFieldVm updateText TextFieldData.Text(colorInput.r)
                gTextFieldVm updateText TextFieldData.Text(colorInput.g)
                bTextFieldVm updateText TextFieldData.Text(colorInput.b)
            }
        }
    }

    private fun collectTextFieldsData() {
        coroutineScope.launch(defaultDispatcher) {
            combine(
                rTextFieldVm.dataFlow,
                gTextFieldVm.dataFlow,
                bTextFieldVm.dataFlow,
            ) { r, g, b ->
                arrayOf(r, g, b)
            }
                .collectLatest collect@{ (r, g, b) ->
                    // don't synchronize this data with other Views to avoid update loop
                    val isAnyCausedByUser = listOf(r, g, b).any { it.text.causedByUser }
                    if (!isAnyCausedByUser) return@collect // none caused by user
                    val parsedColor = TextFieldsDerived(r, g, b).validationResult.getColorOrNull()
                    mediator.set(color = parsedColor, source = DomainColorInputType.Rgb)
                }
        }
    }

    private fun collectSmartBackspacePreference() {
        coroutineScope.launch(defaultDispatcher) {
            userPreferencesRepository.flowOfSmartBackspace
                .filterReady()
                .map { it.getOrElse { DefaultUserPreferences.SmartBackspace } }
                .collectLatest { preference ->
                    compositionNode.update {
                        it.copy(isSmartBackspaceEnabled = preference.enabled)
                    }
                }
        }
    }

    private fun filterUserInput(input: String): TextFieldData.Text =
        input
            .filter { it.isDigit() }
            .take(3) // rgb component can be up to 3 digits long
            .let { string ->
                if (string.isEmpty()) return@let ""
                val rgbComponentMinValue = Color.Rgb.ComponentRange.first
                val rgbComponentMaxValue = Color.Rgb.ComponentRange.last
                var int = string.toIntOrNull() ?: rgbComponentMinValue // remove leading zeros
                // reduce int from right until it's in range
                while (int > rgbComponentMaxValue) {
                    int /= 10
                }
                int.toString()
            }
            .let { TextFieldData.Text(it) }

    private fun submitInput() {
        val derived = TextFieldsDerived(rTextFieldVm.data, gTextFieldVm.data, bTextFieldVm.data)
        val wasAccepted = submitAction.invoke(
            colorInput = derived.colorInput,
            validationResult = derived.validationResult,
        )
        val result = ColorInputSubmissionResult(wasAccepted)
        coroutineScope.launch {
            compositionNode.update {
                it.copy(
                    submitInput = ActionWithResult(
                        result = result,
                        resultAck = ::clearSubmitInputResult,
                        action = ::submitInput,
                    )
                )
            }
        }
    }

    private fun clearSubmitInputResult() {
        coroutineScope.launch {
            compositionNode.update {
                it.copy(
                    submitInput = it.submitInput.copy(result = null),
                )
            }
        }
    }

    private fun createTextFieldViewModel(): TextFieldViewModel =
        textFieldViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
            compositionScope = compositionScope,
            filterUserInput = ::filterUserInput,
            enableClearTextFeature = false,
        )

    override fun dispose() {
        super.dispose()
        rTextFieldVm.dispose()
        gTextFieldVm.dispose()
        bTextFieldVm.dispose()
    }

    private fun TextFieldsDerived(
        r: TextFieldData,
        g: TextFieldData,
        b: TextFieldData,
    ): TextFieldsDerived {
        val colorInput = ColorInput.Rgb(
            r = r.text.data.string,
            g = g.text.data.string,
            b = g.text.data.string,
        )
        val validationResult = with(colorInputValidator) { colorInput.validate() }
        return TextFieldsDerived(
            r = r,
            g = g,
            b = b,
            colorInput = colorInput,
            validationResult = validationResult,
        )
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            compositionScope: CompositionScope,
            mediator: ColorInputMediator,
            submitAction: ColorInputSubmitAction,
        ): ColorInputRgbViewModel
    }

    companion object {
        val EmptyColorInput = ColorInput.Rgb(r = "", g = "", b = "")
    }
}

/**
 * Couples [TextFieldData] of R, G, and B text fields with values that are derived from it.
 */
private data class TextFieldsDerived(
    val r: TextFieldData,
    val g: TextFieldData,
    val b: TextFieldData,
    val colorInput: ColorInput.Rgb,
    val validationResult: ColorInputValidationResult,
)