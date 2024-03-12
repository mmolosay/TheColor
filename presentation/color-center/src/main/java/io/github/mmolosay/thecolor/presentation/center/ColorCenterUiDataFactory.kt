package io.github.mmolosay.thecolor.presentation.center

import io.github.mmolosay.thecolor.presentation.center.ColorCenterUiData.ChangePageButton
import io.github.mmolosay.thecolor.presentation.center.ColorCenterUiData.Page
import io.github.mmolosay.thecolor.presentation.center.ColorCenterUiData.ViewData

fun ColorCenterUiData(
    data: ColorCenterData,
    viewData: ViewData,
): ColorCenterUiData =
    ColorCenterUiData(
        detailsPage = Page(
            changePageButton = ChangePageButton(
                text = viewData.detailsPageChangePageButtonText,
                onClick = { data.changePage(1) },
            ),
        ),
        schemePage = Page(
            changePageButton = ChangePageButton(
                text = viewData.schemePageChangePageButtonText,
                onClick = { data.changePage(0) },
            ),
        ),
        changePageEvent = data.changePageEvent?.toUi(),
    )

private fun ColorCenterData.ChangePageEvent.toUi() =
    ColorCenterUiData.ChangePageEvent(
        destPage = this.destPage,
        onConsumed = this.onConsumed,
    )