package io.github.mmolosay.thecolor.presentation.center

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import io.github.mmolosay.thecolor.presentation.center.ColorCenterUiData.Page
import io.github.mmolosay.thecolor.presentation.center.ColorCenterUiData.Page.ChangePageButton
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
                icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                iconPlacement = ChangePageButton.IconPlacement.Trailing,
            ),
        ),
        schemePage = Page(
            changePageButton = ChangePageButton(
                text = viewData.schemePageChangePageButtonText,
                onClick = { data.changePage(0) },
                icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                iconPlacement = ChangePageButton.IconPlacement.Leading,
            ),
        ),
        changePageEvent = data.changePageEvent?.toUi(),
    )

private fun ColorCenterData.ChangePageEvent.toUi() =
    ColorCenterUiData.ChangePageEvent(
        destPage = this.destPage,
        onConsumed = this.onConsumed,
    )