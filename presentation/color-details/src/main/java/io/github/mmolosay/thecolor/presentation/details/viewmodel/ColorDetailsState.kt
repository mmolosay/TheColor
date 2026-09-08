package io.github.mmolosay.thecolor.presentation.details.viewmodel

import io.github.mmolosay.thecolor.domain.color.Color

sealed interface ColorDetailsState {

    data object Idle : ColorDetailsState

    data class Loading(
        val subjectColor: SubjectColorData,
        val session: ColorDetailsSession?,
        val request: Request,
    ) : ColorDetailsState

    data class Ready(
        val subjectColor: SubjectColorData,
        val data: ColorDetailsData,
        val session: ColorDetailsSession,
    ) : ColorDetailsState

    data class Error(
        val subjectColor: SubjectColorData,
        val error: ColorDetailsError,
        val session: ColorDetailsSession?,
    ) : ColorDetailsState

    data class Request(
        val color: Color,
    )
}

fun ColorDetailsState.subjectColorOrNull(): SubjectColorData? =
    when (this) {
        is ColorDetailsState.Idle -> null
        is ColorDetailsState.Loading -> this.subjectColor
        is ColorDetailsState.Ready -> this.subjectColor
        is ColorDetailsState.Error -> this.subjectColor
    }

fun ColorDetailsState.sessionOrNull(): ColorDetailsSession? =
    when (this) {
        is ColorDetailsState.Idle -> null
        is ColorDetailsState.Loading -> this.session
        is ColorDetailsState.Ready -> this.session
        is ColorDetailsState.Error -> this.session
    }

internal fun ColorDetailsState.isAwaiting(request: ColorDetailsState.Request): Boolean =
    (this is ColorDetailsState.Loading) && (this.request == request)