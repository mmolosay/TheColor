package io.github.mmolosay.thecolor.presentation.details.viewmodel

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.utils.Identity
import io.github.mmolosay.thecolor.domain.color.ColorDetails as DomainColorDetails

sealed interface ColorDetailsState {

    data object NotRequested : ColorDetailsState

    data class Loading(
        val session: Session?,
        val request: Request,
    ) : ColorDetailsState

    data class Ready(
        val details: DomainColorDetails,
        val role: ColorRole,
        val session: Session,
    ) : ColorDetailsState

    data class Error(
        val session: Session?,
        val cause: Throwable,
        val request: Request,
    ) : ColorDetailsState

    data class Request(
        val color: Color,
        val role: ColorRole,
        val identity: Identity = Identity(),
    )

    data class Session(
        val seed: DomainColorDetails,
        val exact: DomainColorDetails?,
    )
}

/**
 * Depicts a role of a color inside the 'Color Details' feature.
 */
enum class ColorRole {
    Seed,
    Exact,
}

fun ColorDetailsState.Session.color(role: ColorRole): Color =
    when (role) {
        ColorRole.Seed -> this.seed.color
        ColorRole.Exact -> this.seed.exact.color
    }

fun ColorDetailsState.Session.details(role: ColorRole): DomainColorDetails? =
    when (role) {
        ColorRole.Seed -> this.seed
        ColorRole.Exact -> this.exact
    }

sealed interface ColorDetailsOutcome {

    data class ColorSelected(
        val color: Color,
    ) : ColorDetailsOutcome
}