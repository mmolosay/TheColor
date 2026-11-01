package io.github.mmolosay.thecolor.presentation.details.viewmodel

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.IsColorLightUseCase
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsData.ColorRoleData
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsData.ExactMatch
import io.github.mmolosay.thecolor.utils.Identity
import javax.inject.Inject
import io.github.mmolosay.thecolor.domain.color.ColorDetails as DomainColorDetails

class ColorDetailsCore @AssistedInject constructor(
    private val createData: CreateColorDetailsDataUseCase,
    private val createSubject: CreateSubjectUseCase,
) {

    fun initialState(): ColorDetailsState =
        ColorDetailsState.NotRequested

    // region Getters

    fun ColorDetailsState.subject(): Color? =
        when (this) {
            is ColorDetailsState.NotRequested -> null
            is ColorDetailsState.Loading -> this.request.color
            is ColorDetailsState.Ready -> this.details.color
            is ColorDetailsState.Error -> this.request.color
        }

    fun ColorDetailsState.awaited(): ColorDetailsState.Request? =
        this
            .let { it as? ColorDetailsState.Loading }
            ?.request

    private fun ColorDetailsState.session(): ColorDetailsState.Session? =
        when (this) {
            is ColorDetailsState.NotRequested -> null
            is ColorDetailsState.Loading -> this.session
            is ColorDetailsState.Ready -> this.session
            is ColorDetailsState.Error -> this.session
        }

    fun ColorDetailsState.toViewData(): ColorDetailsViewData =
        when (this) {
            is ColorDetailsState.NotRequested ->
                ColorDetailsViewData.Empty
            is ColorDetailsState.Loading ->
                ColorDetailsViewData.Loading(
                    subject = createSubject(this.request.color),
                )
            is ColorDetailsState.Ready ->
                ColorDetailsViewData.Ready(
                    subject = createSubject(this.details.color),
                    data = createData(
                        details = this.details,
                        role = this.role,
                        seedColor = this.session.seed.color,
                    ),
                )
            is ColorDetailsState.Error ->
                ColorDetailsViewData.Error(
                    subject = createSubject(this.request.color),
                    cause = this.cause,
                )
        }

    // endregion

    // region Setters

    fun ColorDetailsState.withSession(seed: Color): ColorDetailsState {
        val request = ColorDetailsState.Request(color = seed, role = ColorRole.Seed)
        return ColorDetailsState.Loading(session = null, request = request)
    }

    fun ColorDetailsState.withSeedDetails(details: DomainColorDetails): ColorDetailsState {
        val session = ColorDetailsState.Session(seed = details, exact = null)
        return ColorDetailsState.Ready(details = details, role = ColorRole.Seed, session = session)
    }

    fun ColorDetailsState.withoutSession(): ColorDetailsState =
        ColorDetailsState.NotRequested

    // endregion

    // region Transitions

    fun ColorDetailsState.select(role: ColorRole): Change<ColorDetailsState, ColorDetailsOutcome> {
        val session = this.session() ?: return Change(this)
        val color = session.color(role)
        val details = session.details(role)
        val state = if (details != null) {
            ColorDetailsState.Ready(
                details = details,
                role = role,
                session = session,
            )
        } else {
            val request = ColorDetailsState.Request(color = color, role = role)
            ColorDetailsState.Loading(
                session = session,
                request = request,
            )
        }
        val outcome = ColorDetailsOutcome.ColorSelected(color)
        return Change(state, outcome)
    }

    fun ColorDetailsState.retry(): Change<ColorDetailsState, Nothing> {
        if (this !is ColorDetailsState.Error) return Change(this)
        val newRequest = this.request.copy(identity = Identity())
        val state = ColorDetailsState.Loading(session = this.session, request = newRequest)
        return Change(state)
    }

    fun ColorDetailsState.completeDetails(
        result: Result<DomainColorDetails>,
        expected: ColorDetailsState.Request,
    ): Change<ColorDetailsState, Nothing> {
        if (this.awaited() != expected) return Change(this)
        val session = this.session()
        val details = result.getOrElse { e ->
            val state = ColorDetailsState.Error(session = session, cause = e, request = expected)
            return Change(state)
        }
        val state = when (expected.role) {
            ColorRole.Seed ->
                this.withSeedDetails(details)
            ColorRole.Exact -> {
                val updated = requireNotNull(session).copy(exact = details)
                ColorDetailsState.Ready(
                    details = details,
                    role = ColorRole.Exact,
                    session = updated,
                )
            }
        }
        return Change(state)
    }

    // endregion

    @AssistedFactory
    fun interface Factory {
        fun create(): ColorDetailsCore
    }
}

class CreateColorDetailsDataUseCase @Inject constructor(
    private val colorToColorInt: ColorToColorIntUseCase,
) {

    operator fun invoke(
        details: DomainColorDetails,
        role: ColorRole,
        seedColor: Color,
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
                colorRole = role,
                seedColor = seedColor,
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
        seedColor: Color,
    ): ColorRoleData =
        when (colorRole) {
            ColorRole.Seed -> {
                val exactColor = details.exact.color
                ColorRoleData.Seed(
                    exactColor = with(colorToColorInt) { exactColor.toColorInt() },
                )
            }
            ColorRole.Exact -> {
                ColorRoleData.Exact(
                    seedColor = with(colorToColorInt) { seedColor.toColorInt() },
                )
            }
        }
}

class CreateSubjectUseCase @Inject constructor(
    private val colorToColorInt: ColorToColorIntUseCase,
    private val isColorLight: IsColorLightUseCase,
) {

    operator fun invoke(color: Color): ColorDetailsViewData.Subject =
        ColorDetailsViewData.Subject(
            color = with(colorToColorInt) { color.toColorInt() },
            isDark = with(isColorLight) { color.isLight().not() },
        )
}