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

    override fun setUp() {
        observeValidationState()
    }

    override fun setViews() {
        setInputPager()
        setInputTabs()
        setProcceedBtn()
    }

    private fun setInputPager() = binding.inputPager.run {
        adapter = ColorInputPagerAdapter(this@ColorInputHostFragment)
    }

    private fun setInputTabs() = binding.inputTabs.run {
        TabLayoutMediator(this, binding.inputPager, ::configureInputTab).attach()
    }

    private fun setProcceedBtn() = binding.procceedBtn.run {
        setOnClickListener { colorInputVM.procceedInput() }
    }

    private fun configureInputTab(tab: TabLayout.Tab, position: Int) {
        val data = getFromEnumOrNull<ColorInputPagerAdapter.Tab>(position) ?: return
        tab.setText(data.titleRes)
    }

    private fun observeValidationState() =
        colorInputVM.getColorValidationState().observe(this) { resource ->
            resource.ifSuccess { valid ->
                binding.procceedBtn.isEnabled = valid
            }
        }

    override fun setSoftInputMode() {
        // https://yatmanwong.medium.com/android-how-to-pan-the-page-up-more-25fc5c542a97
    }

    companion object {
        fun newInstance() = ColorInputHostFragment()
    }
}