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
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.input.ColorInputMapper
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.ColorInputSource
import io.github.mmolosay.thecolor.presentation.input.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.model.getColorOrNull
import io.github.mmolosay.thecolor.presentation.input.set
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldDataFactory
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldHandle
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldInputProcessor
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldViewModel
import io.github.mmolosay.thecolor.utils.Lens
import io.github.mmolosay.thecolor.utils.RequiresWriteOrdering
import io.github.mmolosay.thecolor.utils.Store
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
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
    @Assisted private val store: Store<ColorInputRgbData>,
    @Assisted private val mediator: ColorInputMediator,
    @Assisted private val submitAction: ColorInputSubmitAction,
    private val textFieldViewModelFactory: TextFieldViewModel.Factory,
    private val colorInputValidator: ColorInputValidator,
    private val colorInputMapper: ColorInputMapper,
    private val colorConverter: ColorConverter,
    private val userPreferencesRepository: UserPreferencesRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val exclusiveLane = defaultDispatcher.limitedParallelism(1)

    private val rTextFieldViewModel = createTextFieldViewModel(
        lens = Lens(
            get = { s -> s.rTextField },
            set = { s, v -> s.copy(rTextField = v) },
        ),
    )
    val rTextFieldHandle = TextFieldHandle(rTextFieldViewModel)

    private val gTextFieldViewModel = createTextFieldViewModel(
        lens = Lens(
            get = { s -> s.gTextField },
            set = { s, v -> s.copy(gTextField = v) },
        ),
    )
    val gTextFieldHandle = TextFieldHandle(gTextFieldViewModel)

    private val bTextFieldViewModel = createTextFieldViewModel(
        lens = Lens(
            get = { s -> s.bTextField },
            set = { s, v -> s.copy(bTextField = v) },
        ),
    )
    val bTextFieldHandle = TextFieldHandle(bTextFieldViewModel)

    val dataFlow: StateFlow<ColorInputRgbData> = store.flow

    init {
        collectMediatorUpdates()
        collectTextFieldsData()
        collectSmartBackspacePreference()
    }

    private fun collectMediatorUpdates() {
        coroutineScope.launch(defaultDispatcher) {
            mediator.colorStateFlow.collect { (color, source) ->
                // don't update text fields to avoid update loop if the color was set from this 'Color Input' type
                if (source is ColorInputSource && source.type == DomainColorInputType.Rgb) return@collect
                val colorInput = if (color != null) {
                    val hexColor = with(colorConverter) { color.toRgb() }
                    with(colorInputMapper) { hexColor.toColorInput() }
                } else {
                    EmptyColorInput
                }
                @OptIn(RequiresWriteOrdering::class)
                store.transaction {
                    fun String.toTextWithSource() =
                        TextFieldData.Text(this) causedByUser false
                    rTextFieldViewModel.setText(colorInput.r.toTextWithSource())
                    gTextFieldViewModel.setText(colorInput.g.toTextWithSource())
                    bTextFieldViewModel.setText(colorInput.b.toTextWithSource())
                }
            }
        }
    }

    private fun collectTextFieldsData() {
        coroutineScope.launch(defaultDispatcher) {
            store.flow
                .map { data -> TextFieldsDerived(data.rTextField, data.gTextField, data.bTextField) }
                .distinctUntilChangedBy { derived -> derived.color } // only update mediator when color changes
                .collectLatest collect@{ derived ->
                    val r = derived.r; val g = derived.g; val b = derived.b
                    // don't synchronize this data with other Views to avoid update loop
                    val isAnyCausedByUser = listOf(r, g, b).any { it.text.causedByUser }
                    if (!isAnyCausedByUser) return@collect // none caused by user
                    mediator.set(
                        color = derived.color,
                        source = ColorInputSource(DomainColorInputType.Rgb),
                    )
                }
        }
    }

    private fun collectSmartBackspacePreference() {
        coroutineScope.launch(defaultDispatcher) {
            userPreferencesRepository.flowOfSmartBackspace
                .filterReady()
                .map { it.getOrElse { DefaultUserPreferences.SmartBackspace } }
                .collectLatest { preference ->
                    store.update {
                        it.copy(isSmartBackspaceEnabled = preference.enabled)
                    }
                }
        }
    }

    fun execute(action: ColorInputRgbAction): Job =
        coroutineScope.launch(exclusiveLane) {
            when (action) {
                is ColorInputRgbAction.SubmitInput -> {
                    submitInput()
                }
                is ColorInputRgbAction.AckInputSubmissionResult -> {
                    clearInputSubmissionResult()
                }
            }
        }

    private suspend fun submitInput() {
        val data = store.current()
        val derived = TextFieldsDerived(data.rTextField, data.gTextField, data.bTextField)
        val wasAccepted = submitAction.invoke(
            colorInput = derived.colorInput,
            validationResult = derived.validationResult,
        )
        val result = ColorInputSubmissionResult(wasAccepted)
        store.update {
            it.copy(inputSubmissionResult = result)
        }
    }

    private suspend fun clearInputSubmissionResult() {
        store.update {
            it.copy(inputSubmissionResult = null)
        }
    }

    private fun createTextFieldViewModel(
        lens: Lens<ColorInputRgbData, TextFieldData>,
    ): TextFieldViewModel =
        textFieldViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
            store = store.focus(lens),
            inputProcessor = TextFieldInputProcessorImpl(),
        )

    override fun dispose() {
        super.dispose()
        rTextFieldViewModel.dispose()
        gTextFieldViewModel.dispose()
        bTextFieldViewModel.dispose()
    }

    private fun TextFieldsDerived(
        r: TextFieldData,
        g: TextFieldData,
        b: TextFieldData,
    ): TextFieldsDerived {
        val colorInput = ColorInput.Rgb(
            r = r.text.data.string,
            g = g.text.data.string,
            b = b.text.data.string,
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

    private class TextFieldInputProcessorImpl : TextFieldInputProcessor {
        override fun invoke(input: String): TextFieldData.Text =
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
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            store: Store<ColorInputRgbData>,
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

private val TextFieldsDerived.color: Color?
    get() = this.validationResult.getColorOrNull()

class ColorInputRgbDataFactory @Inject constructor(
    private val textFieldDataFactory: TextFieldDataFactory,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    fun create() =
        ColorInputRgbData(
            rTextField = textFieldDataFactory.create(
                text = TextFieldData.Text("") causedByUser false,
                isClearTextFeatureEnabled = false,
            ),
            gTextField = textFieldDataFactory.create(
                text = TextFieldData.Text("") causedByUser false,
                isClearTextFeatureEnabled = false,
            ),
            bTextField = textFieldDataFactory.create(
                text = TextFieldData.Text("") causedByUser false,
                isClearTextFeatureEnabled = false,
            ),
            inputSubmissionResult = null,
            isSmartBackspaceEnabled = userPreferencesRepository.flowOfSmartBackspace
                .value.getOrElse { DefaultUserPreferences.SmartBackspace }
                .enabled,
        )
}