package com.ordolabs.feature_home.ui.fragment.color.data.details

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ordolabs.feature_home.ui.fragment.color.data.ColorDataObtainFragment
import com.ordolabs.feature_home.ui.fragment.color.data.base.BaseColorDataFragment
import com.ordolabs.feature_home.util.FeatureHomeUtil.featureHomeComponent
import com.ordolabs.feature_home.viewmodel.colordata.details.ColorDetailsObtainViewModel
import com.ordolabs.thecolor.model.color.data.ColorDetails
import com.ordolabs.thecolor.util.struct.Resource
import kotlinx.coroutines.flow.Flow

class ColorDetailsObtainFragment :
    ColorDataObtainFragment<ColorDetails>() {

    // independent, brand new ViewModel
    private val colorDetailsObtainVM: ColorDetailsObtainViewModel by viewModels {
        featureHomeComponent.viewModelFactory
    }

    // region ColorDataObtainFragment

    override fun getColorDataFlow(): Flow<Resource<ColorDetails>> =
        colorDetailsObtainVM.details

    override fun obtainColorData() {
        val color = color ?: return
        colorDetailsObtainVM.getColorDetails(color)
    }

    override fun makeColorDataFragment(): BaseColorDataFragment<ColorDetails> =
        ColorDetailsFragment.newInstance(colorDetails = null)

    override fun makeContentShimmerFragment(): Fragment =
        ColorDetailsShimmerFragment.newInstance()

    // endregion

    companion object {
        fun newInstance() =
            ColorDetailsObtainFragment()
    }
}