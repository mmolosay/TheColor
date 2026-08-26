package io.github.mmolosay.thecolor.presentation.details.viewmodel

interface ColorDetailsHandle {
    fun facade(state: ColorDetailsState): ColorDetailsFacade
}

data class ColorDetailsFacade(
    val state: ColorDetailsState,
    val execute: ExecuteColorDetailsAction,
)

fun ColorDetailsHandle(viewModel: ColorDetailsViewModel): ColorDetailsHandle =
    ColorDetailsHandleImpl(
        execute = viewModel::execute,
    )

private class ColorDetailsHandleImpl(
    private val execute: ExecuteColorDetailsAction,
) : ColorDetailsHandle {

    override fun facade(state: ColorDetailsState): ColorDetailsFacade =
        ColorDetailsFacade(
            state = state,
            execute = execute,
        )
}