package io.github.mmolosay.thecolor.presentation.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _dataFlow = MutableStateFlow(initialData())
    val dataFlow = _dataFlow.asStateFlow()

    private fun setPreferredColorInput(value: SettingsData.ColorInputType) {
        _dataFlow.update {
            it.copy(preferredColorInput = value)
        }
    }

    private fun initialData() =
        SettingsData(
            preferredColorInput = SettingsData.ColorInputType.Hex,
            changePreferredColorInput = ::setPreferredColorInput,
        )
}