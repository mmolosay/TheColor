package com.ordolabs.thecolor.ui.fragment.colorinput

import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.michaelbull.result.Result
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ordolabs.thecolor.R
import com.ordolabs.thecolor.databinding.FragmentColorInputHostBinding
import com.ordolabs.thecolor.ui.adapter.pager.ColorInputPagerAdapter
import com.ordolabs.thecolor.ui.fragment.BaseFragment
import com.ordolabs.thecolor.util.ext.findFragmentById
import com.ordolabs.thecolor.util.ext.getFromEnumOrNull
import com.ordolabs.thecolor.viewmodel.ColorInputViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ColorInputHostFragment :
    BaseFragment(R.layout.fragment_color_input_host),
    ColorInputModelFragment {

    private val binding: FragmentColorInputHostBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by viewModel()

    override fun setUp() {
        observeValidationState()
    }

    override fun setViews() {
        setInputPager()
        setInputTabs()
    }

    private fun setInputPager() = binding.inputPager.run {
        adapter = ColorInputPagerAdapter(this@ColorInputHostFragment)
    }

    private fun setInputTabs() = binding.inputTabs.run {
        TabLayoutMediator(this, binding.inputPager, ::configureInputTab).attach()
    }

    private fun configureInputTab(tab: TabLayout.Tab, position: Int) {
        val data = getFromEnumOrNull<ColorInputPagerAdapter.Tab>(position) ?: return
        tab.setText(data.titleRes)
    }

    override fun validateColorInput() {
        val fragment = findFragmentById(R.id.inputPager)
        val inputFragment = fragment as? ColorInputModelFragment
        inputFragment?.validateColorInput()
    }

    override fun processColorInput(): Result<Unit, Boolean> {
        TODO("processColorInput is not implemented")
    }

    private fun observeValidationState() = colorInputVM.colorValidationState.observe(this) { resource ->
        resource.onSuccess { valid ->
            binding.procceed.isEnabled = valid
        }
    }

    companion object {
        fun newInstance() = ColorInputHostFragment()
    }
}