package com.ordolabs.feature_home.ui.fragment.colordata.scheme

import androidx.fragment.app.Fragment
import com.ordolabs.feature_home.ui.fragment.colordata.ColorDataObtainFragment
import com.ordolabs.feature_home.ui.fragment.colordata.base.BaseColorDataFragment
import com.ordolabs.feature_home.ui.fragment.colordata.details.ColorDetailsShimmerFragment
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeObtainViewModel
import com.ordolabs.thecolor.model.ColorSchemePresentation
import com.ordolabs.thecolor.util.struct.Resource
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.viewmodel.ext.android.viewModel

class ColorSchemeObtainFragment :
    ColorDataObtainFragment<ColorSchemePresentation>() {

    // independent, brand new ViewModel
    private val colorSchemeObtainVM: ColorSchemeObtainViewModel by viewModel()

    // region ColorDataObtainFragment

    override fun getColorDataFlow(): Flow<Resource<ColorSchemePresentation>> =
        colorSchemeObtainVM.scheme

    override fun obtainColorData() {
        val seed = color ?: return
        colorSchemeObtainVM.getColorScheme(seed)
    }

    override fun makeColorDataFragment(): BaseColorDataFragment<ColorSchemePresentation> =
        ColorSchemeFragment.newInstance()

    override fun makeContentShimmerFragment(): Fragment =
        ColorDetailsShimmerFragment.newInstance() // TODO: create and use distinct one for scheme

    // endregion

    companion object {
        fun newInstance() =
            ColorSchemeObtainFragment()
    }
}