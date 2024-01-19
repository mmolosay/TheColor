package io.github.mmolosay.thecolor.presentation.home.input.rgb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.home.input.InitialTextProvider
import io.github.mmolosay.thecolor.presentation.home.input.Update
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData.Text
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldViewModel
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldViewModel.Companion.updateWith
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ColorInputRgbViewModel.Factory::class)
class ColorInputRgbViewModel @AssistedInject constructor(
    @Assisted viewData: ColorInputRgbViewData,
    initialTextProvider: InitialTextProvider,
    private val mediator: ColorInputMediator,
    private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val rInputFieldViewModel =
        ColorInputFieldViewModel(
            initialText = initialTextProvider.rgbR,
            viewData = viewData.rInputField,
            filterUserInput = ::filterUserInput,
        )
    private val gInputFieldViewModel =
        ColorInputFieldViewModel(
            initialText = initialTextProvider.rgbG,
            viewData = viewData.gInputField,
            filterUserInput = ::filterUserInput,
        )
    private val bInputFieldViewModel =
        ColorInputFieldViewModel(
            initialText = initialTextProvider.rgbB,
            viewData = viewData.bInputField,
            filterUserInput = ::filterUserInput,
        )

    val uiDataFlow = combine(
        rInputFieldViewModel.uiDataUpdatesFlow,
        gInputFieldViewModel.uiDataUpdatesFlow,
        bInputFieldViewModel.uiDataUpdatesFlow,
    ) { r, g, b ->
        Update(
            data = makeUiData(r.data, g.data, b.data),
            causedByUser = listOf(r, g, b).any { it.causedByUser },
        )
    }
        .onEach(::onEachUiDataUpdate)
        .map { it.data }
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
            mediator.rgbStateFlow.collect { input ->
                rInputFieldViewModel updateWith Text(input.r)
                gInputFieldViewModel updateWith Text(input.g)
                bInputFieldViewModel updateWith Text(input.b)
            }
        }
    }

    private fun onEachUiDataUpdate(update: Update<ColorInputRgbUiData>) {
        if (!update.causedByUser) return // don't synchronize this update with other Views
        val (uiData) = update
        val input = uiData.assembleColorInput()
        mediator.send(input)
    }

    private fun filterUserInput(input: String): Text =
        input
            .filter { it.isDigit() }
            .take(MAX_SYMBOLS_IN_RGB_COMPONENT)
            .let { Text(it) }

    private fun makeInitialUiData() =
        makeUiData(
            rInputFieldViewModel.uiDataUpdatesFlow.value.data,
            gInputFieldViewModel.uiDataUpdatesFlow.value.data,
            bInputFieldViewModel.uiDataUpdatesFlow.value.data,
        )

    private fun makeUiData(
        rInputField: ColorInputFieldUiData,
        gInputField: ColorInputFieldUiData,
        bInputField: ColorInputFieldUiData,
    ) =
        ColorInputRgbUiData(rInputField, gInputField, bInputField)

    @AssistedFactory
    interface Factory {
        fun create(viewData: ColorInputRgbViewData): ColorInputRgbViewModel
    }

    private companion object {
        const val MAX_SYMBOLS_IN_RGB_COMPONENT = 3
    }
}