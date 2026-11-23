package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.center.feature.ColorCenterState
import io.github.mmolosay.thecolor.presentation.details.feature.ColorDetailsState
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorState
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupState
import io.github.mmolosay.thecolor.utils.Identity
import io.github.mmolosay.thecolor.utils.Lens

data class HomeState(
    val colorEditor: ColorEditorState,
    val colorInputGroup: ColorInputGroupState,
    val colorCenter: ColorCenterState,
    val selectedSwatch: ColorDetailsState,
    val swatchSession: SwatchSession?,
    val startupColorRequest: StartupColorRequest?,
    val autoProceed: Boolean?,
    val lastSearchedColorSaveRequest: LastSearchedColorSaveRequest?,
) {

    data class StartupColorRequest(
        val colorEditorVersion: Int,
        val identity: Identity = Identity(),
    )

    data class LastSearchedColorSaveRequest(
        val color: Color,
        val identity: Identity = Identity(),
    )
}

data class SwatchSession(
    val identity: Identity = Identity(),
)

object HomeStateLenses {

    val colorEditor = Lens<HomeState, ColorEditorState>(
        get = { s -> s.colorEditor },
        set = { s, v -> s.copy(colorEditor = v) },
    )
    val colorInputGroup = Lens<HomeState, ColorInputGroupState>(
        get = { s -> s.colorInputGroup },
        set = { s, v -> s.copy(colorInputGroup = v) },
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