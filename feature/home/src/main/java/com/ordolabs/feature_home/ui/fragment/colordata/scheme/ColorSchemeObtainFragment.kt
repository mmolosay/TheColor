package com.ordolabs.feature_home.ui.fragment.colordata.scheme

import androidx.fragment.app.Fragment
import com.ordolabs.feature_home.ui.fragment.colordata.ColorDataObtainFragment
import com.ordolabs.feature_home.ui.fragment.colordata.base.BaseColorDataFragment
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeObtainViewModel
import com.ordolabs.thecolor.model.colordata.ColorScheme
import com.ordolabs.thecolor.util.struct.Resource
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.viewmodel.ext.android.viewModel

class ColorSchemeObtainFragment :
    ColorDataObtainFragment<ColorScheme>() {

    // independent, brand new ViewModel
    private val colorSchemeObtainVM: ColorSchemeObtainViewModel by viewModel()

    // region ColorDataObtainFragment

    override fun getColorDataFlow(): Flow<Resource<ColorScheme>> =
        colorSchemeObtainVM.scheme

    override fun obtainColorData() {
        val seed = color ?: return
        colorSchemeObtainVM.getColorScheme(seed)
    }

    override fun makeColorDataFragment(): BaseColorDataFragment<ColorScheme> =
        ColorSchemeFragment.newInstance()

    override fun makeContentShimmerFragment(): Fragment =
        ColorSchemeShimmerFragment.newInstance()

    // endregion

    companion object {
        fun newInstance() =
            ColorSchemeObtainFragment()
    }
}