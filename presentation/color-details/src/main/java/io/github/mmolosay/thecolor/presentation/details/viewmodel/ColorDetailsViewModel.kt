package io.github.mmolosay.thecolor.presentation.details.viewmodel

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
import io.github.mmolosay.thecolor.domain.color.ColorRepository
import io.github.mmolosay.thecolor.domain.color.IsColorLightUseCase
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.IoDispatcher
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsData.ColorRoleData
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsData.ExactMatch
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
    private val createSubjectColorData: CreateSubjectColorDataUseCase,
    private val colorComparator: ColorComparator,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val _subjectColorDataFlow = MutableStateFlow<SubjectColorData?>(null)
    val subjectColorDataFlow = _subjectColorDataFlow.asStateFlow()

    private val _dataStateFlow = MutableStateFlow<DataState>(DataState.Idle)
    val dataStateFlow = _dataStateFlow.asStateFlow()

    private val fetchOrFindColorDetailsJob = AtomicReference<Job?>(null)

    private val session = AtomicReference<Session?>(null)
    private val entryStore = ColorEntryStore()
    private val cachedDetails = CopyOnWriteArraySet<DomainColorDetails>()

    init {
        collectColorDetailsCommands()
    }

    private fun collectColorDetailsCommands() =
        coroutineScope.launch(defaultDispatcher) {
            commandProvider.commandFlow.collect(::process)
        }

    // TODO: make is so that commands are processed in an individual coroutine thus unblocking successive commands from being processed
    // TODO: cancel ongoing command if a new one of the same type was issued
    private suspend fun process(command: ColorDetailsCommand) {
        when (command) {
            is ColorDetailsCommand.SetSeedColor -> {
                val color = command.color
                session.set(null)
                _subjectColorDataFlow.value = createSubjectColorData(color)
                val details = fetchOrFindColorDetails(color).getOrElse { exception ->
                    val error = ColorDetailsError(
                        cause = exception,
                        tryAgain = {
                            coroutineScope.launch(defaultDispatcher) {
                                process(command)
                            }
                        },
                    )
                    _dataStateFlow.value = DataState.Error(error)
                    return
                }
                setColorDetails(details)
                session.set(Session(seedDetails = details))
            }
            is ColorDetailsCommand.SetSeedDetails -> {
                val details = command.details
                session.set(null)
                _subjectColorDataFlow.value = createSubjectColorData(details.color)
                setColorDetails(details)
                session.set(Session(seedDetails = details))
            }
            is ColorDetailsCommand.SelectColor -> {
                val targetRole = command.colorRole
                val session = session.get()
                require(session != null) { "Session must be initialized" }
                val color = session.getByRole(targetRole)
                val details = fetchOrFindColorDetails(color).getOrElse { exception ->
                    val error = ColorDetailsError(
                        cause = exception,
                        tryAgain = {
                            coroutineScope.launch(defaultDispatcher) {
                                process(command)
                            }
                        },
                    )
                    _dataStateFlow.value = DataState.Error(error)
                    return
                }
                setColorDetails(details)
            }
        }
    }

    private suspend fun fetchOrFindColorDetails(color: Color): Result<DomainColorDetails> {
        val cached = findCachedDetails(color)
        if (cached != null) return Result.success(cached)
        return withContext(ioDispatcher) {
            colorRepository.getColorDetails(color)
        }
    }

//    private fun fetchOrFindColorDetails(color: Color) {
//        coroutineScope.launch(defaultDispatcher) {
//            val colorDetails = run {
//                val cached = findCachedDetails(color)
//                if (cached != null) return@run cached
//                return@run withContext(ioDispatcher) {
//                    colorRepository.getColorDetails(color)
//                }
//                    .getOrElse { exception ->
//                        val error = ColorDetailsError(
//                            cause = exception,
//                            tryAgain = { fetchOrFindColorDetails(color) },
//                        )
//                        _dataStateFlow.value = DataState.Error(error)
//                        return@launch
//                    }
//            }
//            setColorDetails(colorDetails)
//        }.also { job ->
//            fetchOrFindColorDetailsJob.getAndSet(job)?.cancel()
//        }
//    }

    private fun setColorDetails(
        details: DomainColorDetails,
    ) {
        val colorRole = details.color.inferColorRole() ?: error("ColorRole cannot be null here")
        val data = createData(
            details = details,
            colorRole = colorRole,
            goToSeedColor = { seedColor -> sendColorSelectedEvent(seedColor, ColorRole.Seed) },
            goToExactColor = { exactColor -> sendColorSelectedEvent(exactColor, ColorRole.Exact) },
            getSeedColor = { exactColor -> findCachedDetailsWithExactColor(exactColor)?.color },
        )
        _dataStateFlow.value = DataState.Ready(data)
        cachedDetails += details
        entryStore.set(color = details.color, role = colorRole)
        coroutineScope.launch(defaultDispatcher) {
            val event = ColorDetailsEvent.DataFetched(details)
            eventStore.send(event)
        }
    }

    /**
     * Infers the [ColorRole] of the receiver [Color] in the context of the current state of this ViewModel.
     *
     * @return [ColorRole] or `null` if the specified [Color] doesn't relate to the current state.
     */
    private fun Color.inferColorRole(): ColorRole? {
        val color = this
        val existingSeed = entryStore.findWithRole(ColorRole.Seed)
        if (existingSeed == null) {
            return ColorRole.Seed
        }
        if (with(colorComparator) { color isSameAs existingSeed }) {
            return ColorRole.Seed
        }
        val existingExact = entryStore.findWithRole(ColorRole.Exact)
        if (existingExact != null && with(colorComparator) { color isSameAs existingExact }) {
            return ColorRole.Exact
        }
        val detailsOfSeed = requireNotNull(findCachedDetails(color = existingSeed))
        val isExactForSeed = with(colorComparator) { color isSameAs detailsOfSeed.exact.color }
        if (isExactForSeed) {
            return ColorRole.Exact
        }
        return null
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

private class Session(
    val seed: Color,
    val exact: Color,
)

private fun Session(seedDetails: DomainColorDetails) =
    Session(
        seed = seedDetails.color,
        exact = seedDetails.exact.color,
    )

private fun Session.getByRole(role: ColorRole): Color =
    when (role) {
        ColorRole.Seed -> this.seed
        ColorRole.Exact -> this.exact
    }

private class ColorEntryStore {

    private val entries = mutableMapOf<Color, ColorRole>()
    var currentColor: Color? = null
        private set

    @Synchronized
    fun set(color: Color, role: ColorRole) {
        entries[color] = role
        currentColor = color
    }

    @Synchronized
    @Suppress("UnusedVariable")
    fun findWithRole(role: ColorRole): Color? =
        entries.entries
            .find { (entryColor, entryRole) -> entryRole == role }
            ?.key

    @Synchronized
    fun clear() {
        entries.clear()
        currentColor = null
    }
}

@Singleton
/* private but Dagger */
class CreateColorDetailsDataUseCase @Inject constructor(
    private val colorToColorInt: ColorToColorIntUseCase,
) {

    operator fun invoke(
        details: DomainColorDetails,
        colorRole: ColorRole,
        goToSeedColor: GoToSeedColorAction,
        goToExactColor: GoToExactColorAction,
        getSeedColor: GetSeedColorAction,
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
            ),
            colorRoleData = ColorRoleData(
                details = details,
                colorRole = colorRole,
                goToSeedColor = goToSeedColor,
                goToExactColor = goToExactColor,
                getSeedColor = getSeedColor,
            ),
        )

    private fun ExactMatch(
        details: DomainColorDetails,
    ): ExactMatch =
        if (details.matchesExact) {
            ExactMatch.Yes
        } else {
            ExactMatch.No(
                exactValue = details.exact.hexStringWithNumberSign,
                exactColor = with(colorToColorInt) { details.exact.color.toColorInt() },
                deviation = details.distanceFromExact.toString(),
            )
        }

    private fun ColorRoleData(
        details: DomainColorDetails,
        colorRole: ColorRole,
        goToSeedColor: GoToSeedColorAction,
        goToExactColor: GoToExactColorAction,
        getSeedColor: GetSeedColorAction,
    ): ColorRoleData =
        when (colorRole) {
            ColorRole.Seed -> {
                val exactColor = details.exact.color
                ColorRoleData.Seed(
                    exactColor = with(colorToColorInt) { exactColor.toColorInt() },
                    goToExactColor = { goToExactColor(exactColor) },
                )
            }
            ColorRole.Exact -> {
                val seedColor = getSeedColor(exactColor = details.color)
                    .let { requireNotNull(it) }
                ColorRoleData.Exact(
                    seedColor = with(colorToColorInt) { seedColor.toColorInt() },
                    goToSeedColor = { goToSeedColor(seedColor) },
                )
            }
        }

    fun interface GoToSeedColorAction {
        operator fun invoke(seedColor: Color)
    }

    fun interface GoToExactColorAction {
        operator fun invoke(exactColor: Color)
    }

    fun interface GetSeedColorAction {
        operator fun invoke(exactColor: Color): Color?
    }
}

@Singleton
/* private but Dagger */
class CreateSubjectColorDataUseCase @Inject constructor(
    private val colorToColorInt: ColorToColorIntUseCase,
    private val isColorLight: IsColorLightUseCase,
) {

    operator fun invoke(color: Color) =
        SubjectColorData(
            color = with(colorToColorInt) { color.toColorInt() },
            isDark = with(isColorLight) { color.isLight().not() },
        )
}