package io.github.mmolosay.thecolor.presentation.details.viewmodel

sealed interface ColorDetailsState {

    data object Idle : ColorDetailsState

    data class Loading(
        val subjectColor: SubjectColorData,
    ) : ColorDetailsState

    data class Ready(
        val subjectColor: SubjectColorData,
        val data: ColorDetailsData,
    ) : ColorDetailsState

    data class Error(
        val subjectColor: SubjectColorData,
        val error: ColorDetailsError,
    ) : ColorDetailsState
}

fun ColorDetailsState.subjectColorOrNull(): SubjectColorData? =
    when (this) {
        is ColorDetailsState.Idle -> null
        is ColorDetailsState.Loading -> this.subjectColor
        is ColorDetailsState.Ready -> this.subjectColor
        is ColorDetailsState.Error -> this.subjectColor
    }