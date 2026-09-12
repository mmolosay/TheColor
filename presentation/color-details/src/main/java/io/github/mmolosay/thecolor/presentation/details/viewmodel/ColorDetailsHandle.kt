package io.github.mmolosay.thecolor.presentation.details.viewmodel

import kotlinx.coroutines.flow.StateFlow

interface ColorDetailsHandle {
    val stateFlow: StateFlow<ColorDetailsState>
    fun facade(state: ColorDetailsState): ColorDetailsFacade
}

data class ColorDetailsFacade(
    val state: ColorDetailsState,
    val execute: ExecuteColorDetailsAction,
)

fun ColorDetailsHandle(viewModel: ColorDetailsViewModel): ColorDetailsHandle =
    ColorDetailsHandleImpl(
        stateFlow = viewModel.stateFlow,
        execute = viewModel::execute,
    )

private class ColorDetailsHandleImpl(
    override val stateFlow: StateFlow<ColorDetailsState>,
    private val execute: ExecuteColorDetailsAction,
) : ColorDetailsHandle {

    override fun facade(state: ColorDetailsState): ColorDetailsFacade =
        ColorDetailsFacade(
            state = state,
            execute = execute,
        )
}