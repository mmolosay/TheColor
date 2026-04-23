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
import io.github.mmolosay.thecolor.utils.ProcessingRegistry
import io.github.mmolosay.thecolor.utils.removeAndCancelAll
import io.github.mmolosay.thecolor.utils.withRegistry
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.job
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

    private val opRegistry = ProcessingRegistry<Operation>()
    private val session = AtomicReference<Session?>(null)
    private val colorDetailsStore = ColorDetailsStore()

    /**
     * Sets the specified [color] as the "seed" color of this ViewModel.
     * Fetches [DomainColorDetails] for this [color] and sets them into [deferredDetails].
     * Exposes the details (or an error) via [dataStateFlow].
     */
    fun setSeedColor(
        color: Color,
        deferredDetails: CompletableDeferred<DomainColorDetails>? = null,
    ): Job =
        coroutineScope.launch(defaultDispatcher) {
            opRegistry.removeAndCancelAll()
            val operation = Operation.SetSeedColor(color)
            opRegistry.withRegistry(operation, coroutineContext.job) {
                session.set(null)
                _subjectColorDataFlow.value = createSubjectColorData(color)
                val detailsResult = fetchOrFindColorDetails(color)
                deferredDetails?.completeWith(detailsResult)
                val details = detailsResult.getOrElse { exception ->
                    val tryAgain: () -> Unit = {
                        setSeedColor(color)
                    }
                    val error = ColorDetailsError(
                        cause = exception,
                        tryAgain = tryAgain,
                    )
                    _dataStateFlow.value = DataState.Error(error)
                    return@launch
                }
                session.set(Session.fromSeedDetails(seedDetails = details))
                setColorDetails(details)
            }
        }

    /**
     * Same as [setSeedColor], but provides the [details] of the "seed" color to use.
     */
    fun setSeedDetails(details: DomainColorDetails): Job =
        coroutineScope.launch(defaultDispatcher) {
            opRegistry.removeAndCancelAll()
            val operation = Operation.SetSeedDetails(details)
            opRegistry.withRegistry(operation, coroutineContext.job) {
                session.set(null)
                _subjectColorDataFlow.value = createSubjectColorData(details.color)
                session.set(Session.fromSeedDetails(seedDetails = details))
                setColorDetails(details)
            }
        }

    /**
     * Infers a color with the specified [role] in the ongoing color session.
     * Fetches [DomainColorDetails] for that color and sets them into [deferredDetails].
     * Exposes the details (or an error) via [dataStateFlow].
     *
     * Requires the "seed" color to be set.
     */
    fun selectColor(
        role: ColorRole,
        deferredDetails: CompletableDeferred<DomainColorDetails>? = null,
    ): Job =
        coroutineScope.launch(defaultDispatcher) {
            opRegistry.removeAndCancelAll()
            val operation = Operation.SelectColor(role)
            opRegistry.withRegistry(operation, coroutineContext.job) {
                val session = requireNotNull(session.get()) { "Session must be initialized" }
                val color = session.getByRole(role)
                val detailsResult = fetchOrFindColorDetails(color)
                deferredDetails?.completeWith(detailsResult)
                val details = detailsResult.getOrElse { exception ->
                    val tryAgain: () -> Unit = {
                        selectColor(role)
                    }
                    val error = ColorDetailsError(
                        cause = exception,
                        tryAgain = tryAgain,
                    )
                    _dataStateFlow.value = DataState.Error(error)
                    return@launch
                }
                setColorDetails(details)
            }
        }

    private suspend fun fetchOrFindColorDetails(color: Color): Result<DomainColorDetails> {
        val cached = colorDetailsStore.findWithColor(color)
        if (cached != null) return Result.success(cached)
        _dataStateFlow.value = DataState.Loading
        return withContext(ioDispatcher) {
            colorRepository.getColorDetails(color)
        }
    }

    private fun setColorDetails(
        details: DomainColorDetails,
    ) {
        val colorRole = details.color.inferColorRole() ?: error("ColorRole cannot be null here")
        val data = createData(
            details = details,
            colorRole = colorRole,
            selectSeedColor = { seedColor -> sendColorSelectedEvent(seedColor, ColorRole.Seed) },
            selectExactColor = { exactColor -> sendColorSelectedEvent(exactColor, ColorRole.Exact) },
            getSeedColor = { exactColor -> colorDetailsStore.findWithExactColor(exactColor)?.color },
        )
        _dataStateFlow.value = DataState.Ready(data)
        colorDetailsStore.add(details)
    }

    private fun Color.inferColorRole(): ColorRole? {
        val color = this
        val session = requireNotNull(session.get())
        if (with(colorComparator) { color isSameAs session.seed }) {
            return ColorRole.Seed
        }
        if (with(colorComparator) { color isSameAs session.exact }) {
            return ColorRole.Exact
        }
        return null
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
            colorDetailsEventStore: ColorDetailsEventStore,
        ): ColorDetailsViewModel
    }

    /**
     * Describes the current state (color-wise) of the 'Color Details'.
     */
    private class Session(
        val seed: Color,
        val exact: Color,
    ) {
        companion object {
            fun fromSeedDetails(seedDetails: DomainColorDetails) =
                Session(
                    seed = seedDetails.color,
                    exact = seedDetails.exact.color,
                )
        }
    }

    private fun Session.getByRole(role: ColorRole): Color =
        when (role) {
            ColorRole.Seed -> this.seed
            ColorRole.Exact -> this.exact
        }

    /**
     * Directly maps to the public methods of the [ColorDetailsViewModel].
     * Implements "Command" design pattern.
     */
    private sealed interface Operation {

        /**
         * Corresponds to the [ColorDetailsViewModel.setSeedColor] method.
         */
        data class SetSeedColor(
            val color: Color,
        ) : Operation

        /**
         * Corresponds to the [ColorDetailsViewModel.setSeedDetails] method.
         */
        data class SetSeedDetails(
            val details: DomainColorDetails,
        ) : Operation

        /**
         * Corresponds to the [ColorDetailsViewModel.selectColor] method.
         */
        data class SelectColor(
            val role: ColorRole,
        ) : Operation
    }
}

private class ColorDetailsStore {

    private val cachedDetails = CopyOnWriteArraySet<DomainColorDetails>()

    fun add(details: DomainColorDetails) {
        cachedDetails += details
    }

    fun findWithExactColor(exactColor: Color): DomainColorDetails? =
        cachedDetails.find { colorDetails ->
            colorDetails.exact.color == exactColor
        }

    fun findWithColor(color: Color): DomainColorDetails? =
        cachedDetails.find { colorDetails ->
            colorDetails.color == color
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
        selectSeedColor: SelectSeedColorAction,
        selectExactColor: SelectExactColorAction,
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
                selectSeedColor = selectSeedColor,
                selectExactColor = selectExactColor,
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
        selectSeedColor: SelectSeedColorAction,
        selectExactColor: SelectExactColorAction,
        getSeedColor: GetSeedColorAction,
    ): ColorRoleData =
        when (colorRole) {
            ColorRole.Seed -> {
                val exactColor = details.exact.color
                ColorRoleData.Seed(
                    exactColor = with(colorToColorInt) { exactColor.toColorInt() },
                    selectExactColor = { selectExactColor(exactColor) },
                )
            }
            ColorRole.Exact -> {
                val seedColor = getSeedColor(exactColor = details.color)
                    .let { requireNotNull(it) }
                ColorRoleData.Exact(
                    seedColor = with(colorToColorInt) { seedColor.toColorInt() },
                    selectSeedColor = { selectSeedColor(seedColor) },
                )
            }
        }

    fun interface SelectSeedColorAction {
        operator fun invoke(seedColor: Color)
    }

    fun interface SelectExactColorAction {
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