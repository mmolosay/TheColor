package com.ordolabs.feature_home.ui.fragment.colorinput

import android.animation.ObjectAnimator
import androidx.annotation.ColorInt
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentColorInputHostBinding
import com.ordolabs.feature_home.ui.adapter.pager.ColorInputPagerAdapter
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.ColorInputViewModel
import com.ordolabs.thecolor.util.ColorUtil
import com.ordolabs.thecolor.util.ColorUtil.toColorInt
import com.ordolabs.thecolor.util.ext.getFromEnumOrNull
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ColorInputHostFragment : BaseFragment(R.layout.fragment_color_input_host) {

    private val binding: FragmentColorInputHostBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()

    private val defaultPreviewColor: Int by lazy {
        MaterialColors.getColor(binding.root, com.ordolabs.thecolor.R.attr.colorPrimary)
    }

    override fun collectViewModelsData() {
        collectValidationState()
        collectColorPreview()
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

    private fun updateColorPreview(@ColorInt color: Int) {
        val view = binding.preview
        val current = view.cardBackgroundColor.defaultColor
        if (color == current) return
        val duration = resources.getInteger(android.R.integer.config_mediumAnimTime)
        view.setCardBackgroundColor(color)
        ObjectAnimator
            .ofFloat(view, "translationY", 0f, -16f, 15f, -13f, 11f, -7f, 3f, 1f, 0f)
            .setDuration(duration.toLong())
            .start()
    }

    private fun collectValidationState() =
        colorInputVM.colorValidationState.collectOnLifecycle { resource ->
            resource.ifSuccess { valid ->
                binding.procceedBtn.isEnabled = valid
            }
        }

    private fun collectColorPreview() =
        colorInputVM.colorPreview.collectOnLifecycle() { resource ->
            resource.fold(
                onEmpty = ::onColorPreviewEmpty,
                onSuccess = ::onColorPreviewSuccess
            )
        }

    private fun onColorPreviewEmpty() {
        updateColorPreview(defaultPreviewColor)
    }

    private fun onColorPreviewSuccess(color: ColorUtil.Color) {
        updateColorPreview(color.toColorInt())
    }

    companion object {
        fun newInstance() = ColorInputHostFragment()
    }
}