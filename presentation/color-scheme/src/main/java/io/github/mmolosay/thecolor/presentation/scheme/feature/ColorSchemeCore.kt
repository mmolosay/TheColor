package io.github.mmolosay.thecolor.presentation.scheme.feature

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.IsColorLightUseCase
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.utils.Identity
import io.github.mmolosay.thecolor.utils.composition.Change
import kotlinx.collections.immutable.toPersistentList
import javax.inject.Inject
import io.github.mmolosay.thecolor.domain.color.ColorScheme as DomainColorScheme

class ColorSchemeCore @AssistedInject constructor(
    private val createSwatch: CreateSwatchUseCase,
) {

    fun initialState(): ColorSchemeState =
        ColorSchemeState.NotRequested

    // region Getters

    fun ColorSchemeState.awaited(): ColorSchemeState.Request? =
        this
            .let { it as? ColorSchemeState.Requested }
            ?.request

    private fun ColorSchemeState.Ready.hasChangesToApply(): Boolean {
        val hasModeChanged = (this.selectedMode != this.request.mode)
        val hasSwatchCountChanged = (this.selectedSwatchCount != this.request.swatchCount)
        return (hasModeChanged || hasSwatchCountChanged)
    }

    fun ColorSchemeState.toViewData(): ColorSchemeViewData =
        when (this) {
            is ColorSchemeState.NotRequested ->
                ColorSchemeViewData.Empty
            is ColorSchemeState.Requested ->
                ColorSchemeViewData.Loading
            is ColorSchemeState.Ready -> {
                val data = ColorSchemeData(
                    swatches = this.scheme.swatchDetails
                        .map { details -> createSwatch(details.color) }
                        .toPersistentList(),
                    activeMode = this.request.mode,
                    selectedMode = this.selectedMode,
                    activeSwatchCount = this.request.swatchCount,
                    selectedSwatchCount = this.selectedSwatchCount,
                    hasChangesToApply = this.hasChangesToApply(),
                )
                ColorSchemeViewData.Ready(
                    data = data
                )
            }
            is ColorSchemeState.Error ->
                ColorSchemeViewData.Error(
                    cause = this.cause,
                )
        }

    // endregion

    // region Setters

    fun ColorSchemeState.withSession(seed: Color): ColorSchemeState {
        val request = ColorSchemeState.Request(
            seed = seed,
            mode = DomainColorScheme.Mode.Monochrome,
            swatchCount = SwatchCount.Six,
        )
        return ColorSchemeState.Requested(request = request)
    }

    fun ColorSchemeState.withSeed(seed: Color): ColorSchemeState {
        val request = when (this) {
            is ColorSchemeState.NotRequested -> return this
            is ColorSchemeState.Requested -> this.request
            is ColorSchemeState.Ready -> this.request.copy(
                mode = this.selectedMode,
                swatchCount = this.selectedSwatchCount,
            )
            is ColorSchemeState.Error -> this.request
        }
        val newRequest = request.copy(seed = seed, identity = Identity())
        return ColorSchemeState.Requested(request = newRequest)
    }

    fun ColorSchemeState.withoutSession(): ColorSchemeState =
        ColorSchemeState.NotRequested

    // endregion

    // region Transitions

    fun ColorSchemeState.selectSwatch(color: Color): Change<ColorSchemeState, ColorSchemeOutcome> {
        if (this !is ColorSchemeState.Ready) return Change(this)
        val details = this.scheme.swatchDetails
            .find { details -> details.color == color }
            ?: return Change(this)
        val outcome = ColorSchemeOutcome.SwatchSelected(details)
        return Change(this, outcome)
    }

    fun ColorSchemeState.selectMode(mode: DomainColorScheme.Mode): Change<ColorSchemeState, Nothing> {
        if (this !is ColorSchemeState.Ready) return Change(this)
        val state = this.copy(selectedMode = mode)
        return Change(state)
    }

    fun ColorSchemeState.selectSwatchCount(count: SwatchCount): Change<ColorSchemeState, Nothing> {
        if (this !is ColorSchemeState.Ready) return Change(this)
        val state = this.copy(selectedSwatchCount = count)
        return Change(state)
    }

    fun ColorSchemeState.applyChanges(): Change<ColorSchemeState, Nothing> {
        if (this !is ColorSchemeState.Ready) return Change(this)
        if (!this.hasChangesToApply()) return Change(this)
        val state = this.withSeed(this.request.seed)
        return Change(state)
    }

    fun ColorSchemeState.retry(): Change<ColorSchemeState, Nothing> {
        if (this !is ColorSchemeState.Error) return Change(this)
        val state = this.withSeed(this.request.seed)
        return Change(state)
    }

    fun ColorSchemeState.completeScheme(
        result: Result<DomainColorScheme>,
        expected: ColorSchemeState.Request,
    ): Change<ColorSchemeState, Nothing> {
        if (this.awaited() != expected) return Change(this)
        val scheme = result.getOrElse { e ->
            val state = ColorSchemeState.Error(cause = e, request = expected)
            return Change(state)
        }
        val state = ColorSchemeState.Ready(
            scheme = scheme,
            selectedMode = expected.mode,
            selectedSwatchCount = expected.swatchCount,
            request = expected,
        )
        return Change(state)
    }

    // endregion

    @AssistedFactory
    fun interface Factory {
        fun create(): ColorSchemeCore
    }
}

class CreateSwatchUseCase @Inject constructor(
    private val colorToColorInt: ColorToColorIntUseCase,
    private val isColorLight: IsColorLightUseCase,
) {

    operator fun invoke(color: Color): ColorSchemeData.Swatch =
        ColorSchemeData.Swatch(
            color = color,
            colorInt = with(colorToColorInt) { color.toColorInt() },
            isDark = with(isColorLight) { color.isLight().not() },
        )
}