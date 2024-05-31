package io.github.mmolosay.thecolor.presentation.details

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.result.Result
import io.github.mmolosay.thecolor.domain.result.onFailure
import io.github.mmolosay.thecolor.domain.result.onSuccess
import io.github.mmolosay.thecolor.domain.usecase.GetColorDetailsUseCase
import io.github.mmolosay.thecolor.presentation.ColorCenterCommand
import io.github.mmolosay.thecolor.presentation.ColorCenterCommandProvider
import io.github.mmolosay.thecolor.presentation.ColorCenterEvent
import io.github.mmolosay.thecolor.presentation.ColorCenterEventStore
import io.github.mmolosay.thecolor.presentation.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsData.ExactMatch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import io.github.mmolosay.thecolor.domain.model.ColorDetails as DomainColorDetails

/**
 * Not a ViewModel-ViewModel in terms of Android development.
 * It doesn't derive from [androidx.lifecycle.ViewModel], so should only be used in "real" ViewModels
 * which do derive from Android-aware implementation.
 */
class ColorDetailsViewModel @AssistedInject constructor(
    @Assisted private val coroutineScope: CoroutineScope,
    @Assisted private val commandProvider: ColorCenterCommandProvider,
    @Assisted private val eventStore: ColorCenterEventStore,
    private val getColorDetails: GetColorDetailsUseCase,
    private val createData: CreateColorDetailsDataUseCase,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher,
) {

    private val _dataStateFlow =
        MutableStateFlow<State>(State.Idle) // TODO: inject initial state as in ColorSchemeViewModel for better testing
    val dataStateFlow = _dataStateFlow.asStateFlow()

    init {
        collectColorCenterCommands()
    }

    private fun collectColorCenterCommands() =
        coroutineScope.launch { // TODO: not main dispatcher?
            commandProvider.commandFlow.collect { command ->
                when (command) {
                    is ColorCenterCommand.FetchData -> getColorDetails(color = command.color)
                }
            }
        }

    private fun getColorDetails(color: Color) {
        _dataStateFlow.value = State.Loading
        coroutineScope.launch(ioDispatcher) {
            getColorDetails.invoke(color)
                .onSuccess { details ->
                    val data = createData(
                        details = details,
                        onExactClick = ::onExactColorClick,
                    )
                    _dataStateFlow.value = State.Ready(data)
                }
                .onFailure { failure ->
                    _dataStateFlow.value = State.Error(failure)
                }
        }
    }

    private fun onExactColorClick(color: Color) {
        coroutineScope.launch {
            val event = ColorCenterEvent.ExactColorSelected(color)
            eventStore.send(event)
        }
    }

    /** Depicts possible states of color details data. */
    sealed interface State {
        data object Idle : State
        data object Loading : State
        data class Ready(val data: ColorDetailsData) : State
        data class Error(val failure: Result.Failure) : State
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            colorCenterCommandProvider: ColorCenterCommandProvider,
            colorCenterEventStore: ColorCenterEventStore,
        ): ColorDetailsViewModel
    }
}

@Singleton
/* private but Dagger */
class CreateColorDetailsDataUseCase @Inject constructor(
    private val colorToColorInt: ColorToColorIntUseCase,
) {

    operator fun invoke(
        details: DomainColorDetails,
        onExactClick: (color: Color) -> Unit,
    ) =
        ColorDetailsData(
            colorName = details.colorName,
            hex = ColorDetailsData.Hex(value = details.colorHexString.withNumberSign),
            rgb = ColorDetailsData.Rgb(
                r = details.colorTranslations.rgb.standard.r.toString(),
                g = details.colorTranslations.rgb.standard.g.toString(),
                b = details.colorTranslations.rgb.standard.b.toString(),
            ),
            hsl = ColorDetailsData.Hsl(
                h = details.colorTranslations.hsl.standard.h.toString(),
                s = details.colorTranslations.hsl.standard.s.toString(),
                l = details.colorTranslations.hsl.standard.l.toString(),
            ),
            hsv = ColorDetailsData.Hsv(
                h = details.colorTranslations.hsv.standard.h.toString(),
                s = details.colorTranslations.hsv.standard.s.toString(),
                v = details.colorTranslations.hsv.standard.v.toString(),
            ),
            cmyk = ColorDetailsData.Cmyk(
                c = details.colorTranslations.cmyk.standard.c.toString(),
                m = details.colorTranslations.cmyk.standard.m.toString(),
                y = details.colorTranslations.cmyk.standard.y.toString(),
                k = details.colorTranslations.cmyk.standard.k.toString(),
            ),
            exactMatch = ExactMatch(details, onExactClick),
        )

    private fun ExactMatch(
        details: DomainColorDetails,
        onExactClick: (color: Color) -> Unit,
    ): ExactMatch =
        if (details.matchesExact) {
            ExactMatch.Yes
        } else {
            ExactMatch.No(
                exactValue = details.exact.hexStringWithNumberSign,
                exactColor = with(colorToColorInt) { details.exact.color.toColorInt() },
                onExactClick = { onExactClick(details.exact.color) },
                deviation = details.distanceFromExact.toString(),
            )
        }
}