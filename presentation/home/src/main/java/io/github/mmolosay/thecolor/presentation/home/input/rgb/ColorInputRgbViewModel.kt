package io.github.mmolosay.thecolor.presentation.home.input.rgb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.color.ColorPrototype
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData.Text
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldViewModel
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldViewModel.State
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldViewModel.StateReducer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ColorInputRgbViewModel.Factory::class)
class ColorInputRgbViewModel @AssistedInject constructor(
    @Assisted viewData: ColorInputRgbViewData,
    private val mediator: ColorInputMediator,
    private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val rInputFieldViewModel =
        ColorInputFieldViewModel(
            viewData = viewData.rInputField,
            filterUserInput = ::filterUserInput,
        )
    private val gInputFieldViewModel =
        ColorInputFieldViewModel(
            viewData = viewData.gInputField,
            filterUserInput = ::filterUserInput,
        )
    private val bInputFieldViewModel =
        ColorInputFieldViewModel(
            viewData = viewData.bInputField,
            filterUserInput = ::filterUserInput,
        )

    private val rInputFieldStateReducer = StateReducer<ColorPrototype.Rgb> { color ->
        Text(color.r?.toString().orEmpty())
    }
    private val gInputFieldStateReducer = StateReducer<ColorPrototype.Rgb> { color ->
        Text(color.g?.toString().orEmpty())
    }
    private val bInputFieldStateReducer = StateReducer<ColorPrototype.Rgb> { color ->
        Text(color.b?.toString().orEmpty())
    }

    val uiDataFlow = combine(
        rInputFieldViewModel.uiDataFlow,
        gInputFieldViewModel.uiDataFlow,
        bInputFieldViewModel.uiDataFlow,
        ::makeUiData,
    )
        .onEach { uiData ->
            val areAllFieldsEmpty = inputFieldViewModels()
                .all { it.uiDataFlow.value.text.string.isEmpty() }
            val state = if (areAllFieldsEmpty) {
                State.Empty
            } else {
                val prototype = uiData.assembleColorPrototype()
                State.Populated(prototype)
            }
            mediator.send(state)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = makeInitialUiData(),
        )

    init {
        collectMediatorUpdates()
    }

    private fun collectMediatorUpdates() {
        viewModelScope.launch(defaultDispatcher) {
            mediator.rgbStateFlow.collect { state ->
                with(rInputFieldStateReducer) { rInputFieldViewModel apply state }
                with(gInputFieldStateReducer) { gInputFieldViewModel apply state }
                with(bInputFieldStateReducer) { bInputFieldViewModel apply state }
            }
        }
    }

    private fun filterUserInput(input: String): Text =
        input
            .filter { it.isDigit() }
            .take(MAX_SYMBOLS_IN_RGB_COMPONENT)
            .let { Text(it) }

    private fun makeInitialUiData() =
        makeUiData(
            rInputFieldViewModel.uiDataFlow.value,
            gInputFieldViewModel.uiDataFlow.value,
            bInputFieldViewModel.uiDataFlow.value,
        )

    private fun makeUiData(
        rInputField: ColorInputFieldUiData,
        gInputField: ColorInputFieldUiData,
        bInputField: ColorInputFieldUiData,
    ) =
        ColorInputRgbUiData(rInputField, gInputField, bInputField)

    private fun inputFieldViewModels() =
        listOf(rInputFieldViewModel, gInputFieldViewModel, bInputFieldViewModel)

    @AssistedFactory
    interface Factory {
        fun create(viewData: ColorInputRgbViewData): ColorInputRgbViewModel
    }

    private companion object {
        const val MAX_SYMBOLS_IN_RGB_COMPONENT = 3
    }
}