package com.ordolabs.feature_home.ui.fragment

import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.annotation.ColorInt
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.color.MaterialColors
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentHomeBinding
import com.ordolabs.feature_home.di.featureHomeModule
import com.ordolabs.feature_home.ui.fragment.colorinput.ColorInputHostFragment
import com.ordolabs.feature_home.viewmodel.ColorInputViewModel
import com.ordolabs.thecolor.util.ColorUtil
import com.ordolabs.thecolor.util.ColorUtil.toColorInt
import com.ordolabs.thecolor.util.ext.setFragment
import com.ordolabs.thecolor.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.loadKoinModules

class HomeFragment : BaseFragment(R.layout.fragment_home) {

    private val binding: FragmentHomeBinding by viewBinding()
    private val homeVM: HomeViewModel by viewModel()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()

    private val defaultPreviewColor: Int by lazy {
        MaterialColors.getColor(binding.root, com.ordolabs.thecolor.R.attr.colorPrimary)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadKoinModules(featureHomeModule)
    }

    override fun collectViewModelsData() {
        // nothing is here
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

    private fun updateColorPreview(@ColorInt color: Int) {
        val view = binding.preview
        if (color == view.cardBackgroundColor.defaultColor) return
        val duration = resources.getInteger(android.R.integer.config_mediumAnimTime)
        view.setCardBackgroundColor(color)
        ObjectAnimator
            .ofFloat(view, "translationY", 0f, -16f, 15f, -13f, 11f, -7f, 3f, 1f, 0f)
            .setDuration(duration.toLong())
            .start()
    }

    private fun collectColorPreview() =
        colorInputVM.colorPreview.collectOnLifecycle { resource ->
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

    override fun setSoftInputMode() {
        // https://yatmanwong.medium.com/android-how-to-pan-the-page-up-more-25fc5c542a97
    }

    companion object {

        // being created by NavHostFragment, thus no newInstance() method
    }
}