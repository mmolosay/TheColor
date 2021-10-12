package com.ordolabs.feature_home.ui.fragment.colorinput

import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentColorInputHostBinding
import com.ordolabs.feature_home.ui.adapter.pager.ColorInputPagerAdapter
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.ColorInputViewModel
import com.ordolabs.thecolor.util.ext.getFromEnumOrNull
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ColorInputHostFragment : BaseFragment(R.layout.fragment_color_input_host) {

    private val binding: FragmentColorInputHostBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()

    override fun collectViewModelsData() {
        collectColorPreview()
    }

    override fun setViews() {
        setInputPager()
        setInputTabs()
        setProcceedBtn()
    }

    private fun setInputPager() = binding.run {
        val adapter = ColorInputPagerAdapter(this@ColorInputHostFragment)
        inputPager.adapter = adapter
        inputPager.offscreenPageLimit = adapter.itemCount
    }

    private fun setInputTabs() = binding.run {
        TabLayoutMediator(inputTabs, inputPager, ::configureInputTab).attach()
    }

    private fun setProcceedBtn() = binding.run {
        procceedBtn.setOnClickListener {
            colorInputVM.procceedInput()
        }
    }

    private fun configureInputTab(tab: TabLayout.Tab, position: Int) {
        val data = getFromEnumOrNull<ColorInputPagerAdapter.Tab>(position) ?: return
        tab.setText(data.titleRes)
    }

    private fun collectColorPreview() =
        colorInputVM.colorPreview.collectOnLifecycle { resource ->
            binding.procceedBtn.isEnabled = resource.isSuccess
        }

    companion object {
        fun newInstance() = ColorInputHostFragment()
    }
}