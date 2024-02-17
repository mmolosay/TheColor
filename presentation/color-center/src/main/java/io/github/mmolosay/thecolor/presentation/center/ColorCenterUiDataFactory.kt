package io.github.mmolosay.thecolor.presentation.center

import io.github.mmolosay.thecolor.presentation.center.ColorCenterUiData.ChangePageButton
import io.github.mmolosay.thecolor.presentation.center.ColorCenterUiData.Page
import io.github.mmolosay.thecolor.presentation.center.ColorCenterUiData.ViewData

fun ColorCenterUiData(
    data: ColorCenterData,
    viewData: ViewData,
): ColorCenterUiData =
    ColorCenterUiData(
        page = data.page,
        onPageChanged = data.onPageChanged,
        detailsPage = Page(
            changePageButton = ChangePageButton(
                text = viewData.detailsPageChangePageButtonText,
                onClick = { data.changePage(destPage = 1) },
            ),
        ),
        schemePage = Page(
            changePageButton = ChangePageButton(
                text = viewData.schemePageChangePageButtonText,
                onClick = { data.changePage(destPage = 0) },
            ),
        ),
    )