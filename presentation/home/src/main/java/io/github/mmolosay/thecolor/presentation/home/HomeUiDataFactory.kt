package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.presentation.impl.toCompose

fun HomeUiData(
    data: HomeData,
    viewData: HomeUiData.ViewData,
) =
    HomeUiData(
        topBar = TopBar(data, viewData),
        headline = viewData.headline,
        proceedButton = ProceedButton(data, viewData),
        colorPreviewState = ColorPreviewState(data),
        showColorCenter = ShowColorCenter(data.proceedResult),
        invalidSubmittedColorToast = InvalidSubmittedColorToast(data.proceedResult, viewData),
    )

private fun TopBar(
    data: HomeData,
    viewData: HomeUiData.ViewData,
) =
    HomeUiData.TopBar(
        settingsAction = HomeUiData.TopBar.SettingsAction(
            onClick = data.goToSettings,
            iconContentDescription = viewData.settingsIconContentDesc,
        )
    )

private fun ShowColorCenter(result: HomeData.ProceedResult?) =
    when (result) {
        is HomeData.ProceedResult.Success ->
            HomeUiData.ShowColorCenter.Yes(
                backgroundColor = result.colorData.color.toCompose(),
                useLightContentColors = result.colorData.isDark,
            )
        is HomeData.ProceedResult.InvalidSubmittedColor ->
            HomeUiData.ShowColorCenter.No
        null ->
            HomeUiData.ShowColorCenter.No
    }

/*
 * Ideally, we should call 'discard()' when the message is no longer present on UI.
 * However, there's no built-in mechanism for adding "on end" callback to shown Toast.
 */
private fun InvalidSubmittedColorToast(
    result: HomeData.ProceedResult?,
    viewData: HomeUiData.ViewData,
) =
    when (result) {
        is HomeData.ProceedResult.InvalidSubmittedColor ->
            HomeUiData.InvalidSubmittedColorToast(
                message = viewData.invalidSubmittedColorMessage,
                onShown = result.discard,
            )
        else -> null
    }

private fun ProceedButton(
    data: HomeData,
    viewData: HomeUiData.ViewData,
) =
    HomeUiData.ProceedButton(
        onClick = data.canProceed.actionOrNoop(),
        enabled = data.canProceed is HomeData.CanProceed.Yes,
        text = viewData.proceedButtonText,
    )

private fun ColorPreviewState(
    data: HomeData,
) =
    when (data.proceedResult) {
        is HomeData.ProceedResult.Success ->
            HomeUiData.ColorPreviewState.Submitted // 'proceed' action was invoked for some color
        else ->
            HomeUiData.ColorPreviewState.Default // color was not submitted (proceeded with)
    }

private fun HomeData.CanProceed.actionOrNoop(): () -> Unit =
    when (this) {
        is HomeData.CanProceed.Yes -> action
        is HomeData.CanProceed.No -> {
            {} // disabled button isn't clickable, thus no-op won't ever be invoked
        }
    }