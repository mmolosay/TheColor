package io.github.mmolosay.thecolor.presentation.home

import android.content.Context

fun HomeViewData(context: Context) =
    HomeUiData.ViewData(
        title = context.getString(R.string.home_headline),
        proceedButtonText = context.getString(R.string.home_proceed_btn),
    )