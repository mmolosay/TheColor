package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.center.viewmodel.ColorCenterState
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsState
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorState
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupState
import io.github.mmolosay.thecolor.utils.Identity
import io.github.mmolosay.thecolor.utils.Lens
import kotlinx.collections.immutable.ImmutableList

data class HomeState(
    val inputEditor: ColorEditorState,
    val inputGroup: ColorInputGroupState,
    val colorCenter: ColorCenterState,
    val selectedSwatch: ColorDetailsState,
    val startupColorRequest: StartupColorRequest?,
    val autoProceed: Boolean?, // TODO: non-descriptive state via nullability
    val lastSearchedColorSave: LastSearchedColorSave?,
    val submissionRejected: Boolean, // TODO: temporary
    val sideEffects: ImmutableList<HomeData.SideEffect>, // TODO: temporary
) {

    // TODO: maybe all startup initialization should be described by one model?

    data class StartupColorRequest(
        val editorVersion: Int,
        val identity: Identity = Identity(),
    )

    // TODO: refine name
    data class LastSearchedColorSave(
        val color: Color,
        val identity: Identity = Identity(),
    )
}

object HomeStateLenses {

    val inputEditor = Lens<HomeState, ColorEditorState>(
        get = { s -> s.inputEditor },
        set = { s, v -> s.copy(inputEditor = v) },
    )
    val inputGroup = Lens<HomeState, ColorInputGroupState>(
        get = { s -> s.inputGroup },
        set = { s, v -> s.copy(inputGroup = v) },
    )
    val colorCenter = Lens<HomeState, ColorCenterState>(
        get = { s -> s.colorCenter },
        set = { s, v -> s.copy(colorCenter = v) },
    )
    val selectedSwatch = Lens<HomeState, ColorDetailsState>(
        get = { s -> s.selectedSwatch },
        set = { s, v -> s.copy(selectedSwatch = v) },
    )
}