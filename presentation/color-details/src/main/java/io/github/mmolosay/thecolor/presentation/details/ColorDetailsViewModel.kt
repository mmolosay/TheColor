package io.github.mmolosay.thecolor.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.GetColorDetailsUseCase
import io.github.mmolosay.thecolor.presentation.ColorCenterCommandProvider
import io.github.mmolosay.thecolor.presentation.Command
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsData.ColorInt
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsData.ExactMatch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import io.github.mmolosay.thecolor.domain.model.ColorDetails as DomainColorDetails

@HiltViewModel
class ColorDetailsViewModel @Inject constructor(
    private val commandProvider: ColorCenterCommandProvider,
    private val getColorDetails: GetColorDetailsUseCase,
    private val colorConverter: ColorConverter,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _dataStateFlow = MutableStateFlow<State>(State.Loading) // TODO: inject initial state as in ColorSchemeViewModel for better testing
    val dataStateFlow = _dataStateFlow.asStateFlow()

    init {
        collectColorCenterCommands()
    }

    private fun collectColorCenterCommands() =
        viewModelScope.launch { // TODO: not main dispatcher?
            commandProvider.commandFlow.collect { command ->
                when (command) {
                    is Command.FetchData -> getColorDetails(color = command.color)
                }
            }
        }

    private fun getColorDetails(color: Color) {
        viewModelScope.launch(ioDispatcher) {
            val details = getColorDetails.invoke(color)
            val data = ColorDetailsData(details)
            _dataStateFlow.value = State.Ready(data)
        }
    }

    // TODO: extract into separate 'CreateColorDetailsDataUseCase' to simplify testing of VM
    private fun ColorDetailsData(details: DomainColorDetails) =
        ColorDetailsData(
            colorName = details.name,
            hex = ColorDetailsData.Hex(value = details.hexValue),
            rgb = ColorDetailsData.Rgb(
                r = details.rgbR.toString(),
                g = details.rgbG.toString(),
                b = details.rgbB.toString(),
            ),
            hsl = ColorDetailsData.Hsl(
                h = details.hslH.toString(),
                s = details.hslS.toString(),
                l = details.hslL.toString(),
            ),
            hsv = ColorDetailsData.Hsv(
                h = details.hsvH.toString(),
                s = details.hsvS.toString(),
                v = details.hsvV.toString(),
            ),
            cmyk = ColorDetailsData.Cmyk(
                c = details.cmykC.toString(),
                m = details.cmykM.toString(),
                y = details.cmykY.toString(),
                k = details.cmykK.toString(),
            ),
            exactMatch = ExactMatch(details),
        )

    private fun ColorInt(color: Color): ColorInt {
        val hex = with(colorConverter) { color.toHex() }
        return ColorInt(hex = hex.value)
    }

    private fun ExactMatch(details: DomainColorDetails): ExactMatch =
        if (details.isNameMatchExact) {
            ExactMatch.Yes
        } else {
            ExactMatch.No(
                exactValue = details.exactNameHex,
                exactColor = ColorInt(details.exact),
                onExactClick = { /* TODO */ },
                deviation = details.exactNameHexDistance.toString(),
            )
        }

    sealed interface State {
        data object Loading : State
        data class Ready(val data: ColorDetailsData) : State
    }
}