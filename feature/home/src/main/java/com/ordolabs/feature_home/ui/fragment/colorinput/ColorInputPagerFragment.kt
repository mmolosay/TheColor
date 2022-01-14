package com.ordolabs.feature_home.ui.fragment.colorinput

import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentColorInputPagerBinding
import com.ordolabs.feature_home.ui.adapter.pager.ColorInputPagerAdapter
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.colorinput.ColorInputViewModel
import com.ordolabs.feature_home.viewmodel.colorinput.ColorValidatorViewModel
import com.ordolabs.thecolor.util.ext.getFromEnumOrNull
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ColorInputPagerFragment : BaseFragment(R.layout.fragment_color_input_pager) {

    private val binding: FragmentColorInputPagerBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()
    private val colorValidatorVM: ColorValidatorViewModel by sharedViewModel()

    override fun collectViewModelsData() {
        collectColorPreview()
    }

    override fun setViews() {
        setViewPager()
        setTabs()
        setProcceedBtn()
    }

    private fun setViewPager() = binding.run {
        val adapter = ColorInputPagerAdapter(this@ColorInputPagerFragment)
        inputPager.adapter = adapter
        inputPager.offscreenPageLimit = adapter.itemCount
    }

    private fun setTabs() = binding.run {
        TabLayoutMediator(inputTabs, inputPager, ::configureInputTab).attach()
    }

    private fun setProcceedBtn() = binding.run {
        procceedBtn.setOnClickListener {
            colorValidatorVM.procceedInput()
        }
    }

    private fun configureInputTab(tab: TabLayout.Tab, position: Int) {
        val page = getFromEnumOrNull<ColorInputPagerAdapter.Page>(position) ?: return
        tab.setText(page.titleRes)
    }

    private fun collectColorPreview() =
        colorValidatorVM.colorPreview.collectOnLifecycle { resource ->
            binding.procceedBtn.isEnabled = resource.isSuccess
            resource.ifSuccess { preview ->
                colorInputVM.updateColorInput(preview)
            }
        }

    companion object {
        fun newInstance() = ColorInputPagerFragment()
    }
}