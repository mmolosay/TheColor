package com.ordolabs.feature_home.ui.fragment

import android.os.Bundle
import androidx.annotation.ColorInt
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentHomeBinding
import com.ordolabs.feature_home.di.featureHomeModule
import com.ordolabs.feature_home.ui.fragment.colorinput.ColorInputHostFragment
import com.ordolabs.feature_home.viewmodel.ColorInputViewModel
import com.ordolabs.thecolor.util.ColorUtil.Color
import com.ordolabs.thecolor.util.ColorUtil.toColorInt
import com.ordolabs.thecolor.util.ext.setColor
import com.ordolabs.thecolor.util.ext.setFragment
import com.ordolabs.thecolor.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.loadKoinModules
import com.ordolabs.thecolor.R as RApp

class HomeFragment : BaseFragment(R.layout.fragment_home) {

    private val binding: FragmentHomeBinding by viewBinding()
    private val homeVM: HomeViewModel by viewModel()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()

    private val defaultColor: Int by lazy {
        resources.getColor(RApp.color.theme_primary, context?.theme)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadKoinModules(featureHomeModule)
    }

    override fun collectViewModelsData() {
        collectColorPreview()
    }

    override fun setViews() {
        setColorInputFragment()
        setColorInformationFragment()
    }

    private fun setColorInputFragment() {
        val fragment = ColorInputHostFragment.newInstance()
        setFragment(fragment)
    }

    private fun setColorInformationFragment() {
        val fragment = ColorInformationFragment.newInstance()
        setFragment(fragment, binding.colorInfoFragmentContainer.id)
    }

    private fun setColorPreview(@ColorInt color: Int) {
        binding.colorInfoFragmentContainer.background.setColor(color)
    }

    private fun collectColorPreview() =
        colorInputVM.colorPreview.collectOnLifecycle { resource ->
            resource.fold(
                onEmpty = ::onColorPreviewEmpty,
                onSuccess = ::onColorPreviewSuccess
            )
        }

    private fun onColorPreviewEmpty() {
        setColorPreview(defaultColor)
    }

    private fun onColorPreviewSuccess(color: Color) {
        setColorPreview(color.toColorInt())
    }

    companion object {

        // being created by NavHostFragment, thus no newInstance() method
    }
}