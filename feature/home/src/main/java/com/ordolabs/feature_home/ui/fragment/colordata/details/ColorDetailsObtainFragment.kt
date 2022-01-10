package com.ordolabs.feature_home.ui.fragment.colordata.details

import androidx.fragment.app.Fragment
import com.ordolabs.feature_home.ui.fragment.colordata.ColorDataObtainFragment
import com.ordolabs.feature_home.ui.fragment.colordata.base.BaseColorDataFragment
import com.ordolabs.feature_home.viewmodel.colordata.details.ColorDetailsObtainViewModel
import com.ordolabs.thecolor.model.ColorDetailsPresentation
import com.ordolabs.thecolor.util.struct.Resource
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.viewmodel.ext.android.viewModel

class ColorDetailsObtainFragment :
    ColorDataObtainFragment<ColorDetailsPresentation>() {

    // independent, brand new ViewModel
    private val colorDetailsObtainVM: ColorDetailsObtainViewModel by viewModel()

    // region ColorDataObtainFragment

    override fun getColorDataFlow(): Flow<Resource<ColorDetailsPresentation>> =
        colorDetailsObtainVM.details

    override fun obtainColorData() {
        val color = color ?: return
        colorDetailsObtainVM.getColorDetails(color)
    }

    override fun makeColorDataFragment(): BaseColorDataFragment<ColorDetailsPresentation> =
        ColorDetailsFragment.newInstance(colorDetails = null)

    override fun makeContentShimmerFragment(): Fragment =
        ColorDetailsShimmerFragment.newInstance()

    // endregion

    companion object {
        fun newInstance() =
            ColorDetailsObtainFragment()
    }
}