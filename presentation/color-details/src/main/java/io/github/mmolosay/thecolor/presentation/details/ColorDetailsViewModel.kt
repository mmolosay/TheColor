package io.github.mmolosay.thecolor.presentation.details

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.result.onFailure
import io.github.mmolosay.thecolor.domain.result.onSuccess
import io.github.mmolosay.thecolor.domain.usecase.GetColorDetailsUseCase
import io.github.mmolosay.thecolor.domain.usecase.IsColorLightUseCase
import io.github.mmolosay.thecolor.presentation.api.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.api.SimpleViewModel
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
 * Handles presentation logic of the 'Color Details' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
class ColorDetailsViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted private val commandProvider: ColorDetailsCommandProvider,
    @Assisted private val eventStore: ColorDetailsEventStore,
    private val getColorDetails: GetColorDetailsUseCase,
    private val createData: CreateColorDetailsDataUseCase,
    private val createSeedData: CreateSeedDataUseCase,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val _currentSeedDataFlow = MutableStateFlow<ColorDetailsSeedData?>(null)
    val currentSeedDataFlow = _currentSeedDataFlow.asStateFlow()

    private val _dataStateFlow = MutableStateFlow<DataState>(DataState.Idle)
    val dataStateFlow = _dataStateFlow.asStateFlow()

    private var lastFetchDataCommand: ColorDetailsCommand.FetchData? = null
    private val colorHistory = mutableListOf<HistoryRecord>()

    init {
        collectColorCenterCommands()
    }

    private fun collectColorCenterCommands() =
        coroutineScope.launch(defaultDispatcher) {
            commandProvider.commandFlow.collect { command ->
                command.process()
            }
        }

    private fun ColorDetailsCommand.process() = when (this) {
        is ColorDetailsCommand.FetchData -> {
            lastFetchDataCommand = this
            updateCurrentSeedData(color = this.color)
            fetchColorDetails(command = this)
        }
        is ColorDetailsCommand.SetColorDetails -> {
            updateCurrentSeedData(color = this.domainDetails.color)
            setColorDetails(
                domainDetails = this.domainDetails,
                colorRole = null,
            )
        }
    }

    private fun fetchColorDetails(
        command: ColorDetailsCommand.FetchData,
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
                    setColorDetails(domainDetails, colorRole)
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

    private fun setColorDetails(
        domainDetails: DomainColorDetails,
        colorRole: ColorRole?,
    ) {
        val data = createData(domainDetails, colorRole)
        _dataStateFlow.value = DataState.Ready(data)
        colorHistory += HistoryRecord(domainDetails, colorRole)
        coroutineScope.launch(defaultDispatcher) {
            val event = ColorDetailsEvent.DataFetched(domainDetails)
            eventStore.send(event)
        }
    }

    private fun updateCurrentSeedData(color: Color) {
        _currentSeedDataFlow.value = createSeedData(color)
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
            val event = ColorDetailsEvent.ColorSelected(color, colorRole)
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
            colorDetailsCommandProvider: ColorDetailsCommandProvider,
            colorDetailsEventStore: ColorDetailsEventStore,
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

@Singleton
/* private but Dagger */
class CreateSeedDataUseCase @Inject constructor(
    private val colorToColorInt: ColorToColorIntUseCase,
    private val isColorLight: IsColorLightUseCase,
) {

    operator fun invoke(color: Color) =
        ColorDetailsSeedData(
            color = with(colorToColorInt) { color.toColorInt() },
            isDark = with(isColorLight) { color.isLight().not() },
        )
}