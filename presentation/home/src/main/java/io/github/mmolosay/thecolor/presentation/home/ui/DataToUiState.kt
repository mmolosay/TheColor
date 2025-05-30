package io.github.mmolosay.thecolor.presentation.home.ui

import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState
import io.github.mmolosay.thecolor.presentation.preview.toUiState

/*
 * This file contains functions that translate
 * data provided by ViewModels
 * to ->
 * simple, UI-oriented values that describe how this data will be presented by Views.
 *
 * Returned values must be backed up by Views (presented accordingly in UI).
 */

internal fun isColorPreviewVisible(
    data: ColorPreviewData,
): Boolean =
    data.toUiState() is ColorPreviewUiState.Visible

internal fun isColorCenterVisible(
    proceedResult: HomeData.ProceedResult?,
): Boolean =
    proceedResult is HomeData.ProceedResult.Success