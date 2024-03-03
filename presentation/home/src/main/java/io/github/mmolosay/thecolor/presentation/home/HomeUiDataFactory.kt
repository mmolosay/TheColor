package io.github.mmolosay.thecolor.presentation.home

fun HomeUiData(
    data: HomeData,
    viewData: HomeUiData.ViewData,
) =
    HomeUiData(
        headline = viewData.headline,
        proceedButton = ProceedButton(data, viewData),
    )

private fun ProceedButton(
    data: HomeData,
    viewData: HomeUiData.ViewData,
) =
    HomeUiData.ProceedButton(
        onClick = data.canProceed.actionOrNoop(),
        enabled = data.canProceed is HomeData.CanProceed.Yes,
        text = viewData.proceedButtonText,
    )

private fun HomeData.CanProceed.actionOrNoop(): () -> Unit =
    when (this) {
        is HomeData.CanProceed.Yes -> action
        is HomeData.CanProceed.No -> {
            {} // disabled button isn't clickable, thus no-op won't ever be invoked
        }
    }