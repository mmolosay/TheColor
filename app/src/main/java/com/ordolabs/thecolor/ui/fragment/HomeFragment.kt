package com.ordolabs.thecolor.ui.fragment

import androidx.annotation.ColorInt
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.thecolor.R
import com.ordolabs.thecolor.databinding.FragmentHomeBinding
import com.ordolabs.thecolor.ui.fragment.colorinput.ColorInputHostFragment
import com.ordolabs.thecolor.util.ColorUtil.toColorInt
import com.ordolabs.thecolor.util.ext.setColor
import com.ordolabs.thecolor.util.ext.setFragment
import com.ordolabs.thecolor.viewmodel.ColorInputViewModel
import com.ordolabs.thecolor.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : BaseFragment(R.layout.fragment_home) {

    private val binding: FragmentHomeBinding by viewBinding()
    private val homeVM: HomeViewModel by viewModel()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()

    override fun setUp() {
        observeColorPreview()
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

    private fun observeColorPreview() =
        colorInputVM.getColorPreview().observe(this) { resource ->
            resource.ifSuccess { color ->
                setColorPreview(color.toColorInt())
            }
        }

    companion object {

        // being created by NavHostFragment, thus no newInstance() method
    }
}