package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.presentation.center.viewmodel.ColorCenterFeature
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsFeature
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorHandle
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupFeature
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData

// TODO: temporary: what Home's View reads until Home is finished
data class HomeViewState(
    val home: HomeData,
    val colorEditorHandle: ColorEditorHandle,
    val colorInputGroup: ColorInputGroupFeature,
    val colorPreview: ColorPreviewData,
    val colorCenter: ColorCenterFeature,
    val selectedSwatch: ColorDetailsFeature,
)
