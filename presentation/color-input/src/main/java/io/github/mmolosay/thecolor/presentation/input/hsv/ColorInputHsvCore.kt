package io.github.mmolosay.thecolor.presentation.input.hsv

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.color.isSameAs

class ColorInputHsvCore @AssistedInject constructor(
    private val colorConverter: ColorConverter,
    private val colorComparator: ColorComparator,
) {

    fun initialState(color: Color?): ColorInputHsvState {
        val hsv = with(colorConverter) { color?.toHsv() }
        return ColorInputHsvState.Settled(
            color = hsv,
            version = 0,
        )
    }

    // region Getters

    fun ColorInputHsvState.awaitedPromotion(): ColorInputHsvState.Picked.Promotion? =
        this
            .let { it as? ColorInputHsvState.Picked }
            ?.promotion

    fun ColorInputHsvState.toViewData(): ColorInputHsvViewData =
        ColorInputHsvViewData(
            color = this.picked().orDefault(),
        )

    private fun ColorInputHsvState.picked(): Color.Hsv? =
        when (this) {
            is ColorInputHsvState.Settled -> this.color
            is ColorInputHsvState.Picked -> this.picked
        }

    // endregion

    // region Setters

    fun ColorInputHsvState.withColor(color: Color?): ColorInputHsvState {
        if (context(colorComparator) { color isSameAs this.color }) return this
        val hsv = with(colorConverter) { color?.toHsv() }
        return ColorInputHsvState.Settled(
            color = hsv,
            version = if (hsv == this.picked()) this.version else (this.version + 1)
        )
    }

    // endregion

    // region Transitions

    fun ColorInputHsvState.pick(
        newColor: (current: Color.Hsv) -> Color.Hsv,
    ): Change<ColorInputHsvState, Nothing> {
        val current = this.picked().orDefault()
        val picked = newColor(current)
        if (picked == this.picked()) return Change(this)
        val version = this.version + 1
        val state = when (this) {
            is ColorInputHsvState.Settled -> ColorInputHsvState.Picked(
                picked = picked,
                color = this.color,
                promotion = ColorInputHsvState.Picked.Promotion(),
                version = version,
            )
            is ColorInputHsvState.Picked ->
                this.copy(picked = picked, version = version)
        }
        return Change(state)
    }

    fun ColorInputHsvState.completePromotion(
        expected: ColorInputHsvState.Picked.Promotion,
    ): Change<ColorInputHsvState, Nothing> {
        return when (this) {
            is ColorInputHsvState.Settled -> Change(this)
            is ColorInputHsvState.Picked -> {
                if (this.promotion != expected) return Change(this)
                val state = ColorInputHsvState.Settled(
                    color = this.picked,
                    version = this.version
                )
                Change(state)
            }
        }
    }

    // endregion

    private fun Color.Hsv?.orDefault(): Color.Hsv =
        this ?: Color.Hsv(
            hue = Color.Hsv.HueRange.start,
            saturation = Color.Hsv.SaturationRange.endInclusive,
            value = Color.Hsv.ValueRange.endInclusive,
        )

    @AssistedFactory
    fun interface Factory {
        fun create(): ColorInputHsvCore
    }
}