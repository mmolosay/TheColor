package io.github.mmolosay.thecolor.input.hex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.input.ColorInputMediator
import io.github.mmolosay.thecolor.input.SharingStartedEagerlyAnd
import io.github.mmolosay.thecolor.input.field.TextFieldData
import io.github.mmolosay.thecolor.input.field.TextFieldData.Text
import io.github.mmolosay.thecolor.input.field.TextFieldViewModel
import io.github.mmolosay.thecolor.input.field.TextFieldViewModel.Companion.updateWith
import io.github.mmolosay.thecolor.input.model.DataState
import io.github.mmolosay.thecolor.input.model.Update
import io.github.mmolosay.thecolor.input.model.asDataState
import io.github.mmolosay.thecolor.input.model.map
import io.github.mmolosay.thecolor.utils.onEachNotNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ColorInputHexViewModel @Inject constructor(
    private val mediator: ColorInputMediator,
    @Named("uiDataUpdateDispatcher") private val uiDataUpdateDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val textFieldVm =
        TextFieldViewModel(
            filterUserInput = ::filterUserInput,
        )

    val dataStateFlow: StateFlow<DataState<ColorInputHexData>> =
        textFieldVm.dataUpdatesFlow
            .map { it?.map(::makeData) }
            .onEachNotNull(::onEachUiDataUpdate)
            .map { it?.data.asDataState() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStartedEagerlyAnd(WhileSubscribed(5000)),
                initialValue = DataState.BeingInitialized,
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

    private fun onEachUiDataUpdate(update: Update<ColorInputHexData>) {
        if (!update.causedByUser) return // don't synchronize this update with other Views
        val input = update.data.assembleColorInput()
        viewModelScope.launch(uiDataUpdateDispatcher) {
            mediator.send(input)
        }
    }

    private fun filterUserInput(input: String): Text =
        input
            .uppercase()
            .filter { it.isDigit() || it in 'A'..'F' }
            .take(MAX_SYMBOLS_IN_HEX_COLOR)
            .let { Text(it) }

    private fun makeData(textField: TextFieldData) =
        ColorInputHexData(
            textField = textField,
        )

    private companion object {
        const val MAX_SYMBOLS_IN_HEX_COLOR = 6
    }
}