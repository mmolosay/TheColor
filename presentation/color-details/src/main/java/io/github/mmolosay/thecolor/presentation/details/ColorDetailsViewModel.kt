package io.github.mmolosay.thecolor.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.GetColorDetailsUseCase
import io.github.mmolosay.thecolor.domain.usecase.IsColorLightUseCase
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
    private val getColorDetails: GetColorDetailsUseCase,
    private val colorConverter: ColorConverter,
    private val isColorLight: IsColorLightUseCase,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _dataState = MutableStateFlow<State>(State.Loading)
    val dataState = _dataState.asStateFlow()

    fun getColorDetails(color: Color) {
        viewModelScope.launch(ioDispatcher) {
            val details = getColorDetails.invoke(color)
            val data = ColorDetailsData(details)
            _dataState.value = State.Ready(data)
        }
    }

    private fun onViewColorSchemeClick() {
        // TODO: implement
    }

    private fun ColorDetailsData(details: DomainColorDetails) =
        ColorDetailsData(
            color = ColorInt(details.color),
            colorName = details.name,
            useLightContentColors = useLightContentColors(details.color),
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
            onViewColorSchemeClick = ::onViewColorSchemeClick,
        )

    private fun ColorInt(color: Color): ColorInt {
        val hex = with(colorConverter) { color.toAbstract().toHex() }
        return ColorInt(hex = hex.value)
    }

    private fun useLightContentColors(backgroundColor: Color): Boolean {
        val isBackgroundLight = with(isColorLight) { backgroundColor.isLight() }
        return !isBackgroundLight // dark content on light and vice versa
    }

    private fun ExactMatch(details: DomainColorDetails): ExactMatch =
        if (details.isNameMatchExact) {
            ExactMatch.Yes
        } else {
            ExactMatch.No(
                exactValue = details.exactNameHex,
                exactColor = ColorInt(details.exact),
                onExactClick = {},
                deviation = details.exactNameHexDistance.toString(),
            )
        }

    sealed interface State {
        data object Loading : State
        data class Ready(val data: ColorDetailsData) : State
    }
}