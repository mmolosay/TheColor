package io.github.mmolosay.thecolor.presentation.home.input.rgb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputMediator.Command
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData.Text
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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

    val uiDataFlow = combine(
        rInputFieldViewModel.uiDataFlow,
        gInputFieldViewModel.uiDataFlow,
        bInputFieldViewModel.uiDataFlow,
        ::makeUiData,
    )
        .onEach { uiData ->
            val areNotAllFieldsEmpty =
                inputFieldViewModels().any { it.uiDataFlow.value.text.string.isNotEmpty() }
            val command = if (areNotAllFieldsEmpty) {
                val prototype = uiData.assembleColorPrototype()
                Command.Populate(prototype)
            } else {
                Command.Clear
            }
            mediator.issue(command)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = makeInitialUiData(),
        )

    init {
        collectMediatorColorFlow()
    }

    private fun collectMediatorColorFlow() {
        viewModelScope.launch(defaultDispatcher) {
            mediator.rgbCommandFlow.collect { command ->
                when (command) {
                    is Command.Clear -> inputFieldViewModels().forEach { it.clearText() }
                    is Command.Populate -> {
                        val rText = Text(command.color.r?.toString().orEmpty())
                        val gText = Text(command.color.g?.toString().orEmpty())
                        val bText = Text(command.color.b?.toString().orEmpty())
                        rInputFieldViewModel.setText(rText)
                        gInputFieldViewModel.setText(gText)
                        bInputFieldViewModel.setText(bText)
                    }
                }
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