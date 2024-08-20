package io.github.mmolosay.thecolor.presentation.details

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.result.onFailure
import io.github.mmolosay.thecolor.domain.result.onSuccess
import io.github.mmolosay.thecolor.domain.usecase.GetColorDetailsUseCase
import io.github.mmolosay.thecolor.presentation.impl.ColorCenterCommand
import io.github.mmolosay.thecolor.presentation.impl.ColorCenterCommandProvider
import io.github.mmolosay.thecolor.presentation.impl.ColorCenterEvent
import io.github.mmolosay.thecolor.presentation.impl.ColorCenterEventStore
import io.github.mmolosay.thecolor.presentation.impl.ColorRole
import io.github.mmolosay.thecolor.presentation.impl.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsData.ExactMatch
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsData.InitialColorData
import io.github.mmolosay.thecolor.presentation.errors.toErrorType
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
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) {

    private val _dataStateFlow = MutableStateFlow<DataState>(DataState.Idle)
    val dataStateFlow = _dataStateFlow.asStateFlow()

    private var lastFetchDataCommand: ColorCenterCommand.FetchData? = null
    private val colorHistory = mutableListOf<HistoryRecord>()

    init {
        collectColorCenterCommands()
    }

    private fun collectColorCenterCommands() =
        coroutineScope.launch(defaultDispatcher) {
            commandProvider.commandFlow.collect { command ->
                when (command) {
                    is ColorCenterCommand.FetchData -> {
                        lastFetchDataCommand = command
                        fetchColorDetails(command)
                    }
                }
            }
        }

    private fun fetchColorDetails(
        command: ColorCenterCommand.FetchData,
    ) =
        fetchColorDetails(
            color = command.color,
            colorRole = command.colorRole,
        )

    private fun fetchColorDetails(
        color: Color,
        colorRole: ColorRole?,
    ) {
        _dataStateFlow.value = DataState.Loading
        coroutineScope.launch(ioDispatcher) {
            getColorDetails.invoke(color)
                .onSuccess { domainDetails ->
                    val data = createData(domainDetails, colorRole)
                    _dataStateFlow.value = DataState.Ready(data)
                    colorHistory += HistoryRecord(domainDetails, colorRole)
                }
                .onFailure { failure ->
                    val error = ColorDetailsError(
                        type = failure.toErrorType(),
                        tryAgain = ::onErrorAction,
                    )
                    _dataStateFlow.value = DataState.Error(error)
                }
        }
    }

    private fun createData(
        domainDetails: DomainColorDetails,
        colorRole: ColorRole?,
    ): ColorDetailsData {
        val color = domainDetails.color
        val exactColor = domainDetails.exact.color
        val initialColor = if (colorRole == ColorRole.Exact) {
            val details = findDetailsOfInitialColor(exactColor = color)
            details?.color
        } else null
        val goToInitialColor =
            if (colorRole == ColorRole.Exact && initialColor != null) {
                { sendColorSelectedEvent(initialColor, ColorRole.Initial) }
            } else null
        return createData(
            details = domainDetails,
            goToExactColor = { sendColorSelectedEvent(exactColor, ColorRole.Exact) },
            initialColor = initialColor,
            goToInitialColor = goToInitialColor,
        )
    }

    /**
     * Given that [exactColor] is an "exact" color,
     * returns [DomainColorDetails] of the last color that had [exactColor] as its "exact" color.
     */
    private fun findDetailsOfInitialColor(exactColor: Color): DomainColorDetails? =
        colorHistory
            .reversed() // search in order from most recent to last
            .find { (colorDetails, colorRole) ->
                val hasProperColorRole = colorRole in listOf(ColorRole.Initial, null)
                val hasMatchingExactColor = (colorDetails.exact.color == exactColor)
                return@find (hasProperColorRole && hasMatchingExactColor)
            }
            ?.colorDetails

    private fun sendColorSelectedEvent(
        color: Color,
        colorRole: ColorRole,
    ) {
        coroutineScope.launch(defaultDispatcher) {
            val event = ColorCenterEvent.ColorSelected(color, colorRole)
            eventStore.send(event)
        }
    }

    private fun onErrorAction() {
        val command = requireNotNull(lastFetchDataCommand)
        fetchColorDetails(command)
    }

    sealed interface DataState {
        data object Idle : DataState
        data object Loading : DataState
        data class Ready(val data: ColorDetailsData) : DataState
        data class Error(val error: ColorDetailsError) : DataState
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            colorCenterCommandProvider: ColorCenterCommandProvider,
            colorCenterEventStore: ColorCenterEventStore,
        ): ColorDetailsViewModel
    }

    private data class HistoryRecord(
        val colorDetails: DomainColorDetails,
        val colorRole: ColorRole?,
    )
}

@Singleton
/* private but Dagger */
class CreateColorDetailsDataUseCase @Inject constructor(
    private val colorToColorInt: ColorToColorIntUseCase,
) {

    operator fun invoke(
        details: DomainColorDetails,
        goToExactColor: () -> Unit,
        initialColor: Color?,
        goToInitialColor: (() -> Unit)?,
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
            exactMatch = ExactMatch(
                details = details,
                goToExactColor = goToExactColor,
            ),
            initialColorData = if (details.matchesExact) { // set 'InitialColorData' only for "exact" colors
                InitialColorData(
                    initialColor = initialColor,
                    goToInitialColor = goToInitialColor,
                )
            } else null,
        )

    private fun ExactMatch(
        details: DomainColorDetails,
        goToExactColor: () -> Unit,
    ): ExactMatch =
        if (details.matchesExact) {
            ExactMatch.Yes
        } else {
            ExactMatch.No(
                exactValue = details.exact.hexStringWithNumberSign,
                exactColor = with(colorToColorInt) { details.exact.color.toColorInt() },
                goToExactColor = goToExactColor,
                deviation = details.distanceFromExact.toString(),
            )
        }

    private fun InitialColorData(
        initialColor: Color?,
        goToInitialColor: (() -> Unit)?,
    ): InitialColorData? {
        initialColor ?: return null
        goToInitialColor ?: return null
        return InitialColorData(
            initialColor = with(colorToColorInt) { initialColor.toColorInt() },
            goToInitialColor = goToInitialColor,
        )
    }
}