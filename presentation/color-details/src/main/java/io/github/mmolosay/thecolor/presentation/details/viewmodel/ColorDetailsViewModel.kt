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
import io.github.mmolosay.thecolor.utils.Store
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
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
    @Assisted private val store: Store<ColorDetailsState>,
    @Assisted private val eventHandler: ColorDetailsEventHandler,
    private val colorRepository: ColorRepository,
    private val createData: CreateColorDetailsDataUseCase,
    private val createSubjectColorData: CreateSubjectColorDataUseCase,
    private val colorComparator: ColorComparator,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    val stateFlow: StateFlow<ColorDetailsState> = store.flow

    private val exclusiveLane = defaultDispatcher.limitedParallelism(1)
    private val session = AtomicReference<Session?>(null)
    private val colorDetailsStore = ColorDetailsStore()

    fun execute(action: ColorDetailsAction): Job =
        coroutineScope.launch(exclusiveLane) {
            when (action) {
                is ColorDetailsAction.SelectColor -> {
                    onSelectColor(role = action.role)
                }
                is ColorDetailsAction.RetryOnError -> {
                    onRetryOnError()
                }
            }
        }

    private fun onSelectColor(role: ColorRole) {
        val session = session.get() ?: return
        val color = session.getByRole(role)
        val event = ColorDetailsEvent.SelectColorAction(color, role)
        eventHandler.offer(event)
    }

    private fun onRetryOnError() {
        val event = ColorDetailsEvent.RetryOnErrorAction
        eventHandler.offer(event)
    }

    /**
     * Sets the specified [color] as the "seed" color of this ViewModel.
     * Fetches [DomainColorDetails] for this [color] and sets them into [deferredDetails].
     * Exposes the details (or an error) via [stateFlow].
     */
    suspend fun setSeedColor(
        color: Color,
        deferredDetails: CompletableDeferred<DomainColorDetails>? = null,
    ) {
        session.set(null)
        val subjectColor = createSubjectColorData(color)
        val detailsResult = colorDetailsStore.findWithColor(color)
            ?.let { Result.success(it) }
            ?: run {
                store.update { ColorDetailsState.Loading(subjectColor) }
                fetchColorDetails(color)
            }
        deferredDetails?.completeWith(detailsResult)
        val details = detailsResult.getOrElse { exception ->
            val error = ColorDetailsError(
                cause = exception,
                origin = ColorDetailsError.Origin.SetSeedColor(color),
            )
            store.update {
                ColorDetailsState.Error(
                    subjectColor = subjectColor,
                    error = error,
                )
            }
            return
        }
        session.set(Session.fromSeedDetails(seedDetails = details))
        setColorDetails(details, subjectColor)
    }

    /**
     * Same as [setSeedColor], but provides the [details] of the "seed" color to use.
     */
    suspend fun setSeedDetails(details: DomainColorDetails) {
        session.set(null)
        val subjectColor = createSubjectColorData(details.color)
        session.set(Session.fromSeedDetails(seedDetails = details))
        setColorDetails(details, subjectColor)
    }

    /**
     * Infers a color with the specified [role] in the ongoing color session.
     * Fetches [DomainColorDetails] for that color and sets them into [deferredDetails].
     * Exposes the details (or an error) via [stateFlow].
     *
     * Requires the "seed" color to be set.
     */
    suspend fun selectColor(
        role: ColorRole,
        deferredDetails: CompletableDeferred<DomainColorDetails>? = null,
    ) {
        val session = requireNotNull(session.get()) { "Session must be initialized" }
        val color = session.getByRole(role)
        val subjectColor = createSubjectColorData(session.seed)
        val detailsResult = colorDetailsStore.findWithColor(color)
            ?.let { Result.success(it) }
            ?: run {
                store.update { ColorDetailsState.Loading(subjectColor) }
                fetchColorDetails(color)
            }
        deferredDetails?.completeWith(detailsResult)
        val details = detailsResult.getOrElse { exception ->
            val error = ColorDetailsError(
                cause = exception,
                origin = ColorDetailsError.Origin.SelectColor(role),
            )
            store.update {
                ColorDetailsState.Error(
                    subjectColor = subjectColor,
                    error = error,
                )
            }
            return
        }
        setColorDetails(details, subjectColor)
    }

    suspend fun retryOnError() {
        val state = store.current()
        if (state !is ColorDetailsState.Error) return
        when (val target = state.error.origin) {
            is ColorDetailsError.Origin.SetSeedColor -> setSeedColor(target.color)
            is ColorDetailsError.Origin.SelectColor -> selectColor(target.role)
        }
    }

    private suspend fun fetchColorDetails(color: Color): Result<DomainColorDetails> =
        withContext(ioDispatcher) {
            colorRepository.getColorDetails(color)
        }

    private suspend fun setColorDetails(
        details: DomainColorDetails,
        subjectColor: SubjectColorData,
    ) {
        val colorRole = details.color.inferColorRole() ?: error("ColorRole cannot be null here")
        val data = createData(
            details = details,
            colorRole = colorRole,
            getSeedColor = { exactColor -> colorDetailsStore.findWithExactColor(exactColor)?.color },
        )
        store.update {
            ColorDetailsState.Ready(
                subjectColor = subjectColor,
                data = data,
            )
        }
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

    private fun ColorDetailsEventHandler.offer(event: ColorDetailsEvent) {
        if (!coroutineScope.isActive) return
        this.invoke(event)
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            store: Store<ColorDetailsState>,
            eventHandler: ColorDetailsEventHandler,
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
        getSeedColor: GetSeedColorAction,
    ): ColorRoleData =
        when (colorRole) {
            ColorRole.Seed -> {
                val exactColor = details.exact.color
                ColorRoleData.Seed(
                    exactColor = with(colorToColorInt) { exactColor.toColorInt() },
                )
            }
            ColorRole.Exact -> {
                val seedColor = getSeedColor(exactColor = details.color)
                    .let { requireNotNull(it) }
                ColorRoleData.Exact(
                    seedColor = with(colorToColorInt) { seedColor.toColorInt() },
                )
            }
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