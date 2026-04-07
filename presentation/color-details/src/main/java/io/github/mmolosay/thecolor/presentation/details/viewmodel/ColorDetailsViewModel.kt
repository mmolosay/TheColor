package io.github.mmolosay.thecolor.presentation.details.viewmodel

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorRepository
import io.github.mmolosay.thecolor.domain.color.IsColorLightUseCase
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.IoDispatcher
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsData.ExactMatch
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsData.InitialColorData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton
import io.github.mmolosay.thecolor.domain.color.ColorDetails as DomainColorDetails

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
    private val colorRepository: ColorRepository,
    private val createData: CreateColorDetailsDataUseCase,
    private val createSeedData: CreateSeedDataUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val _currentSeedDataFlow = MutableStateFlow<ColorDetailsSeedData?>(null)
    val currentSeedDataFlow = _currentSeedDataFlow.asStateFlow()

    private val _dataStateFlow = MutableStateFlow<DataState>(DataState.Idle)
    val dataStateFlow = _dataStateFlow.asStateFlow()

    private val fetchOrFindColorDetailsJob = AtomicReference<Job?>(null)

    private val entryStore = ColorEntryStore()
    private val cachedDetails = CopyOnWriteArraySet<DomainColorDetails>()

    init {
        collectColorDetailsCommands()
    }

    private fun collectColorDetailsCommands() =
        coroutineScope.launch(defaultDispatcher) {
            commandProvider.commandFlow.collect { command ->
                command.process()
            }
        }

    private fun ColorDetailsCommand.process() = when (this) {
        is ColorDetailsCommand.FetchData -> {
            _currentSeedDataFlow.value = createSeedData(this.color)
            fetchOrFindColorDetails(command = this)
        }
        is ColorDetailsCommand.SetColorDetails -> {
            _currentSeedDataFlow.value = createSeedData(this.domainDetails.color)
            setColorDetails(this.domainDetails)
        }
    }

    private fun fetchOrFindColorDetails(
        command: ColorDetailsCommand.FetchData,
    ) {
        val (color, _) = command
        coroutineScope.launch(defaultDispatcher) {
            val colorDetails = run {
                val cached = findCachedDetails(color)
                if (cached != null) return@run cached
                return@run withContext(ioDispatcher) {
                    colorRepository.getColorDetails(color)
                }
                    .getOrElse { exception ->
                        val error = ColorDetailsError(
                            cause = exception,
                            tryAgain = { fetchOrFindColorDetails(command) },
                        )
                        _dataStateFlow.value = DataState.Error(error)
                        return@launch
                    }
            }
            setColorDetails(colorDetails)
        }.also { job ->
            fetchOrFindColorDetailsJob.getAndSet(job)?.cancel()
        }
    }

    private fun setColorDetails(
        details: DomainColorDetails,
    ) {
        val entryType = details.inferEntryType()
        val data = createData(details, entryType)
        _dataStateFlow.value = DataState.Ready(data)
        cachedDetails += details
        run {
            val entry = ColorEntry(color = details.color, type = entryType)
            entryStore.set(entry)
        }
        coroutineScope.launch(defaultDispatcher) {
            val event = ColorDetailsEvent.DataFetched(details)
            eventStore.send(event)
        }
    }

    private fun DomainColorDetails.inferEntryType(): ColorEntry.Type {
        val details = this
        val existingSeed = entryStore.findOfType(ColorEntry.Type.Seed)
        if (existingSeed == null) {
            return ColorEntry.Type.Seed
        }
        if (existingSeed.color == details.color) {
            return ColorEntry.Type.Seed
        }
        val existingExact = entryStore.findOfType(ColorEntry.Type.Exact)
        if (existingExact?.color == details.color) {
            return ColorEntry.Type.Exact
        }
        val detailsOfSeed = requireNotNull(findCachedDetails(color = existingSeed.color))
        val isExactForSeed = (details.color == detailsOfSeed.exact.color)
        if (isExactForSeed) {
            return ColorEntry.Type.Exact
        }
        error("Cannot infer ColorOrigin")
    }

    private fun createData(
        domainDetails: DomainColorDetails,
        entryType: ColorEntry.Type,
    ): ColorDetailsData {
        val color = domainDetails.color
        val exactColor = domainDetails.exact.color
        val initialColor = if (entryType == ColorEntry.Type.Exact) {
            val details = findCachedDetailsWithExactColor(exactColor = color)
            details?.color
        } else null
        val goToInitialColor =
            if (entryType == ColorEntry.Type.Exact && initialColor != null) {
                { sendColorSelectedEvent(initialColor, ColorRole.Initial) }
            } else null
        return createData(
            details = domainDetails,
            goToExactColor = { sendColorSelectedEvent(exactColor, ColorRole.Exact) },
            initialColor = initialColor,
            goToInitialColor = goToInitialColor,
        )
    }

    private fun findCachedDetailsWithExactColor(exactColor: Color): DomainColorDetails? =
        cachedDetails.find { colorDetails ->
            colorDetails.exact.color == exactColor
        }

    private fun findCachedDetails(color: Color): DomainColorDetails? =
        cachedDetails.find { colorDetails ->
            colorDetails.color == color
        }

    private fun sendColorSelectedEvent(
        color: Color,
        colorRole: ColorRole,
    ) {
        coroutineScope.launch(defaultDispatcher) {
            val event = ColorDetailsEvent.ColorSelected(color, colorRole)
            eventStore.send(event)
        }
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
}

// TODO: rename?
private data class ColorEntry(
    val color: Color,
    val type: Type,
) {
    enum class Type {
        Seed, Exact,
    }
}

private class ColorEntryStore {

    private val entries = mutableMapOf<ColorEntry.Type, Color>()
    var currentType: ColorEntry.Type? = null
    val currentEntry: ColorEntry?
        get() {
            val currentType = currentType ?: return null
            val color = entries.getValue(currentType)
            return ColorEntry(color = color, type = currentType)
        }

    @Synchronized
    fun set(entry: ColorEntry) {
        entries[entry.type] = entry.color
        currentType = entry.type
    }

    fun findOfType(type: ColorEntry.Type): ColorEntry? {
        val color = entries[type] ?: return null
        return ColorEntry(color = color, type = type)
    }
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