package io.github.mmolosay.thecolor.input.hex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.input.ColorInputMediator
import io.github.mmolosay.thecolor.input.SharingStartedEagerlyAnd
import io.github.mmolosay.thecolor.input.field.TextFieldUiData
import io.github.mmolosay.thecolor.input.field.TextFieldUiData.Text
import io.github.mmolosay.thecolor.input.field.TextFieldUiData.ViewData
import io.github.mmolosay.thecolor.input.field.TextFieldViewModel
import io.github.mmolosay.thecolor.input.field.TextFieldViewModel.Companion.updateWith
import io.github.mmolosay.thecolor.input.model.Update
import io.github.mmolosay.thecolor.input.model.map
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Named

@HiltViewModel(assistedFactory = ColorInputHexViewModel.Factory::class)
class ColorInputHexViewModel @AssistedInject constructor(
    @Assisted viewData: ViewData,
    private val mediator: ColorInputMediator,
    @Named("uiDataUpdateDispatcher") private val uiDataUpdateDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val textFieldVm =
        TextFieldViewModel(
            viewData = viewData,
            filterUserInput = ::filterUserInput,
        )

    val uiDataFlow: StateFlow<ColorInputHexUiData?> =
        textFieldVm.uiDataUpdatesFlow
            .filterNotNull()
            .map { it.map(::makeUiData) }
            .onEach(::onEachUiDataUpdate)
            .map { it.data }
            .stateIn(
                scope = viewModelScope,
                started = SharingStartedEagerlyAnd(WhileSubscribed(5000)),
                initialValue = null,
            )

    init {
        collectMediatorUpdates()
    }

    private fun collectMediatorUpdates() {
        viewModelScope.launch(uiDataUpdateDispatcher) {
            mediator.hexColorInputFlow.collect { input ->
                textFieldVm updateWith Text(input.string)
            }
        }
    }

    private fun onEachUiDataUpdate(update: Update<ColorInputHexUiData>) {
        if (!update.causedByUser) return // don't synchronize this update with other Views
        val uiData = update.data
        val input = uiData.assembleColorInput()
        viewModelScope.launch(uiDataUpdateDispatcher) {
            mediator.send(input)
        }
    }

    private fun filterUserInput(input: String): Text =
        input
            .filter { it.isDigit() || it in 'A'..'F' }
            .take(MAX_SYMBOLS_IN_HEX_COLOR)
            .let { Text(it) }

    private fun makeUiData(textField: TextFieldUiData) =
        ColorInputHexUiData(textField)

    private companion object {
        const val MAX_SYMBOLS_IN_HEX_COLOR = 6
    }

    @AssistedFactory
    interface Factory {
        fun create(viewData: ViewData): ColorInputHexViewModel
    }
}