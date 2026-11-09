package io.github.mmolosay.thecolor.presentation.center.viewmodel

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCore
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsOutcome
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeCore
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeOutcome

class ColorCenterCore @AssistedInject constructor(
    detailsFactory: ColorDetailsCore.Factory,
    schemeFactory: ColorSchemeCore.Factory,
) {

    val detailsCore = detailsFactory.create()
    val schemeCore = schemeFactory.create()

    fun initialState(): ColorCenterState =
        ColorCenterState(
            details = detailsCore.initialState(),
            scheme = schemeCore.initialState(),
        )

    // region Getters

    fun ColorCenterState.sessionColor(): Color? =
        with(detailsCore) { details.subject() }

    // endregion

    // region Setters

    fun ColorCenterState.withSession(seed: Color): ColorCenterState =
        ColorCenterState(
            details = with(detailsCore) { details.withSession(seed) },
            scheme = with(schemeCore) { scheme.withSession(seed) },
        )

    fun ColorCenterState.withoutSession(): ColorCenterState =
        ColorCenterState(
            details = with(detailsCore) { details.withoutSession() },
            scheme = with(schemeCore) { scheme.withoutSession() },
        )

    // endregion

    // region Transitions

    fun ColorCenterState.onDetails(
        outcome: ColorDetailsOutcome?,
    ): Change<ColorCenterState, ColorCenterOutcome> =
        when (outcome) {
            null -> Change(this)
            is ColorDetailsOutcome.ColorSelected -> {
                val state = this.copy(
                    scheme = with(schemeCore) { scheme.withSeed(outcome.color) },
                )
                val outcome = ColorCenterOutcome.SessionColorSelected(outcome.color)
                Change(state, outcome)
            }
        }

    fun ColorCenterState.onScheme(
        outcome: ColorSchemeOutcome?,
    ): Change<ColorCenterState, ColorCenterOutcome> =
        when (outcome) {
            null -> Change(this)
            is ColorSchemeOutcome.SwatchSelected -> {
                val outcome = ColorCenterOutcome.SwatchSelected(outcome.details)
                Change(this, outcome)
            }
        }

    // endregion

    @AssistedFactory
    fun interface Factory {
        fun create(): ColorCenterCore
    }
}
