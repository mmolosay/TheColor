package com.ordolabs.feature_home.ui.fragment.color.input

import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorInputPagerFragmentBinding
import com.ordolabs.feature_home.ui.adapter.pager.ColorInputPagerAdapter
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.colorinput.ColorInputViewModel
import com.ordolabs.feature_home.viewmodel.colorinput.ColorValidatorViewModel
import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.ui.util.itemdecoration.MarginDecoration
import com.ordolabs.thecolor.util.ext.getFromEnumOrNull
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import com.ordolabs.thecolor.R as RApp

/**
 * `Fragment` with `ViewPager` which contains `Fragment`s of specific color scheme inputs.
 *
 * Collects [ColorValidatorViewModel.colorPreview] and passes it in [ColorInputViewModel],
 * where they being converted into specific color schemes and collected by child `Fragment`s.
 */
class ColorInputPagerFragment : BaseFragment(R.layout.color_input_pager_fragment) {

    private val binding: ColorInputPagerFragmentBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()
    private val colorValidatorVM: ColorValidatorViewModel by sharedViewModel()

    override fun collectViewModelsData() {
        collectColorPreview()
    }

    override fun setViews() {
        setViewPager()
        setTabs()
    }

    private fun setViewPager() = binding.run {
        val adapter = ColorInputPagerAdapter(this@ColorInputPagerFragment)
        val decoration = MarginDecoration.Horizontal(
            resources,
            RApp.dimen.offset_content_horizontal
        )
        pager.adapter = adapter
        pager.offscreenPageLimit = adapter.itemCount
        pager.addItemDecoration(decoration)
    }

    private fun setTabs() = binding.run {
        TabLayoutMediator(tabs, pager, ::configureInputTab).attach()
    }

    private fun configureInputTab(tab: TabLayout.Tab, position: Int) {
        val page = getFromEnumOrNull<ColorInputPagerAdapter.Page>(position) ?: return
        tab.setText(page.titleRes)
    }

    private fun collectColorPreview() =
        colorValidatorVM.colorPreview.collectOnLifecycle { resource ->
            resource.fold(
                onEmpty = ::onColorPreviewEmpty,
                onSuccess = ::onColorPreviewSuccess
            )
        }

    @Suppress("UNUSED_PARAMETER")
    private fun onColorPreviewEmpty(previous: Color?) {
        colorInputVM.clearColorInput()
    }

    private fun onColorPreviewSuccess(preview: Color) {
        colorInputVM.updateColorInput(preview)
    }

    companion object {
        fun newInstance() = ColorInputPagerFragment()
    }
}