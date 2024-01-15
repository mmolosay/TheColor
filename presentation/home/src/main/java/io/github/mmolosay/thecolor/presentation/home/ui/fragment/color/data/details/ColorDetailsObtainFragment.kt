package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.details

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.ColorDataObtainFragment
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.base.BaseColorDataFragment
import io.github.mmolosay.thecolor.presentation.home.viewmodel.color.data.details.ColorDetailsObtainViewModel
import io.github.mmolosay.thecolor.presentation.color.Color
import io.github.mmolosay.thecolor.presentation.color.data.ColorDetails
import io.github.mmolosay.thecolor.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow

@AndroidEntryPoint
class ColorDetailsObtainFragment :
    ColorDataObtainFragment<ColorDetails>(),
    ColorDetailsObtainView {

    private var details: ColorDetails? = null

    private val colorDetailsObtainVM: ColorDetailsObtainViewModel by viewModels()

    // region Set up

    override fun setUp() {
        parseArguments()
        details?.let { colorDetailsObtainVM.setColorDetails(it) }
    }

    private fun parseArguments() {
        val args = arguments ?: return
        parseColorDetails(args)
    }

    private fun parseColorDetails(args: Bundle) {
        val key = ARGUMENT_KEY_COLOR_DETAILS
        if (!args.containsKey(key)) return
        this.details = args.getParcelable(key)
    }

    // endregion

    // region ColorDataObtainFragment

    override fun getColorDataFlow(): Flow<Resource<ColorDetails>> =
        colorDetailsObtainVM.details

    override fun obtainColorData() {
        if (details != null) return
        val color = color ?: return
        obtainColorDetails(color)
    }

    override fun makeColorDataFragment(): BaseColorDataFragment<ColorDetails> =
        ColorDetailsFragment.newInstance(colorDetails = details)

    override fun makeContentShimmerFragment(): Fragment =
        ColorDetailsShimmerFragment.newInstance()

    // endregion

    // region ColorDetailsObtainView

    override fun obtainColorDetails(color: Color) {
        colorDetailsObtainVM.getColorDetails(color)
    }

    // endregion

    companion object {

        private const val ARGUMENT_KEY_COLOR_DETAILS = "ARGUMENT_KEY_COLOR_DETAILS"

        fun newInstance(
            details: ColorDetails?
        ) =
            ColorDetailsObtainFragment().apply {
                arguments = bundleOf(
                    ARGUMENT_KEY_COLOR_DETAILS to details
                )
            }
    }
}