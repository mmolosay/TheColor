package com.ordolabs.feature_home.ui.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Point
import android.os.Bundle
import android.view.ViewAnimationUtils
import android.view.animation.AnticipateOvershootInterpolator
import androidx.annotation.ColorInt
import androidx.core.animation.doOnStart
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.color.MaterialColors
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentHomeBinding
import com.ordolabs.feature_home.di.featureHomeModule
import com.ordolabs.feature_home.ui.fragment.colorinput.ColorInputHostFragment
import com.ordolabs.feature_home.viewmodel.ColorInputViewModel
import com.ordolabs.thecolor.util.ColorUtil
import com.ordolabs.thecolor.util.ColorUtil.toColorInt
import com.ordolabs.thecolor.util.InsetsUtil
import com.ordolabs.thecolor.util.ext.getBottomVisibleInParent
import com.ordolabs.thecolor.util.ext.getDistanceInParent
import com.ordolabs.thecolor.util.ext.longAnimDuration
import com.ordolabs.thecolor.util.ext.mediumAnimDuration
import com.ordolabs.thecolor.util.ext.setFragment
import com.ordolabs.thecolor.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.loadKoinModules
import kotlin.math.hypot
import com.ordolabs.thecolor.R as RApp

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
        collectColorPreview()
        collectProcceedCommand()
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
        setFragment(fragment, binding.infoFragmentContainer.id)
    }

    private fun updateColorPreview(@ColorInt color: Int) {
        val view = binding.preview
        if (color == view.cardBackgroundColor.defaultColor) return
        view.setCardBackgroundColor(color)
        ObjectAnimator
            .ofFloat(view, "translationY", 0f, -16f, 15f, -13f, 11f, -7f, 3f, 1f, 0f)
            .setDuration(mediumAnimDuration)
            .start()
    }

    private fun animOnProcceedCommand(@ColorInt color: Int) {
        AnimatorSet().apply {
            playSequentially(
                makePreviewHidingAnimation(),
                makeInfoSheetRevealAnimation(color)
            )
        }.start()
    }

    @SuppressLint("Recycle")
    private fun makePreviewHidingAnimation(): Animator {
        val sheet = binding.infoSheet
        val preview = binding.preview
        val distance = preview.getDistanceInParent(sheet, view)?.y ?: 0
        val addend = makeInfoSheetRevealStartPosistion().y
        val radius = preview.height / 2
        val translation = distance.toFloat() + addend - radius
        return ObjectAnimator
            .ofFloat(preview, "translationY", 0f, translation)
            .apply {
                duration = longAnimDuration
                interpolator = AnticipateOvershootInterpolator()
            }
    }

    private fun makeInfoSheetRevealAnimation(@ColorInt color: Int): Animator {
        val sheet = binding.infoSheet
        val preview = binding.preview
        val reveal = makeInfoSheetRevealStartPosistion()
        val sr = preview.width.toFloat() / 2
        val er = hypot(reveal.x.toDouble(), reveal.y.toDouble()).toFloat()
        return ViewAnimationUtils.createCircularReveal(sheet, reveal.x, reveal.y, sr, er).apply {
            duration = longAnimDuration
            interpolator = FastOutSlowInInterpolator()
            doOnStart {
                sheet.isVisible = true
                sheet.backgroundTintList = ColorStateList.valueOf(color)
            }
        }
    }

    private fun makeInfoSheetRevealStartPosistion(): Point {
        val sheet = binding.infoSheet
        val bottom = sheet.getBottomVisibleInParent(view) ?: sheet.height
        val padding = resources.getDimensionPixelSize(RApp.dimen.offset_32)
        val navbarHeight = InsetsUtil.getNavigationBarHeight(context) ?: 0
        val x = sheet.width / 2
        val y = bottom - navbarHeight - padding
        return Point(x, y)
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

    private fun collectProcceedCommand() =
        colorInputVM.procceedCommand.collectOnLifecycle { resource ->
            resource.fold(
                onSuccess = ::onProcceedCommandSuccess
            )
        }

    private fun onProcceedCommandSuccess(color: ColorUtil.Color) {
        animOnProcceedCommand(color.toColorInt())
    }

    override fun setSoftInputMode() {
        // https://yatmanwong.medium.com/android-how-to-pan-the-page-up-more-25fc5c542a97
    }

    companion object {

        // being created by NavHostFragment, thus no newInstance() method
    }
}