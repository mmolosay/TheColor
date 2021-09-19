package com.ordolabs.feature_home.ui.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isInvisible
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
import com.ordolabs.thecolor.util.ext.hideSoftInput
import com.ordolabs.thecolor.util.ext.longAnimDuration
import com.ordolabs.thecolor.util.ext.mediumAnimDuration
import com.ordolabs.thecolor.util.ext.setFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.core.context.loadKoinModules
import kotlin.math.hypot
import com.ordolabs.thecolor.R as RApp

class HomeFragment : BaseFragment(R.layout.fragment_home) {

    private val binding: FragmentHomeBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()

    private val defaultPreviewColor: Int by lazy {
        MaterialColors.getColor(binding.root, com.ordolabs.thecolor.R.attr.colorPrimary)
    }

    private val defaultSheetColor: Int by lazy {
        binding.infoSheet.backgroundTintList?.defaultColor ?: 0
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
        defaultSheetColor // init
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
        val sheetColor = getInfoSheetColor()
        if (color == getPreviewColor()) return
        if (color != sheetColor && sheetColor != defaultSheetColor) {
            animInfoSheetHiding(color)
        } else {
            setPreviewColor(color)
            makePreviewColorChangingAnimation().start()
        }
    }

    private fun animInfoSheetShowing(@ColorInt color: Int) {
        AnimatorSet().apply {
            playSequentially(
                animPreviewHiding(),
                makeInfoSheetRevealShowAnimation().apply {
                    doOnStart {
                        setInfoSheetColor(color)
                    }
                }
            )
        }.start()
    }

    private fun animInfoSheetHiding(@ColorInt color: Int) {
        AnimatorSet().apply {
            playSequentially(
                makeInfoSheetRevealHideAnimation(),
                animPreviewShowing(),
                makePreviewColorChangingAnimation().apply {
                    doOnStart {
                        setPreviewColor(color)
                    }
                }
            )
        }.start()
    }

    private fun animPreviewShowing() = AnimatorSet().apply {
        playTogether(
            makePreviewShowingAnimation(),
            makePreviewElevationAnimation(forward = false)
        )
        duration = 5000L
    }

    private fun animPreviewHiding() = AnimatorSet().apply {
        playTogether(
            makePreviewHidingAnimation(),
            makePreviewElevationAnimation(forward = true)
        )
        duration = 5000L
    }

    private fun makePreviewColorChangingAnimation(): Animator {
        val preview = binding.preview
        return ObjectAnimator
            .ofFloat(preview, "translationY", 0f, -16f, 15f, -13f, 11f, -7f, 3f, 1f, 0f)
            .apply {
                duration = mediumAnimDuration
            }
    }

    private fun makePreviewShowingAnimation(): Animator {
        val preview = binding.preview
        return ObjectAnimator
            .ofFloat(preview, "translationY", preview.translationY, 0f)
            .apply {
                interpolator = AnticipateOvershootInterpolator()
            }
    }

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
                interpolator = AnticipateOvershootInterpolator()
            }
    }

    private fun makePreviewElevationAnimation(forward: Boolean): Animator {
        val preview = binding.preview
        val elevation = resources.getDimension(R.dimen.home_preview_elevation)
        val animator = if (forward) { // reverse() can't be used when is a part of AnimatorSet
            ValueAnimator.ofFloat(elevation, 0f)
        } else {
            ValueAnimator.ofFloat(0f, elevation)
        }
        return animator.apply {
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                Log.d("QWERTY", (animatedValue as Float).toString())
                preview.elevation = animatedValue as Float
            }
        }
    }

    private fun makeInfoSheetRevealHideAnimation(): Animator {
        val sheet = binding.infoSheet
        val preview = binding.preview
        val reveal = makeInfoSheetRevealStartPosistion()
        val sr = hypot(reveal.x.toDouble(), reveal.y.toDouble()).toFloat()
        val er = preview.width.toFloat() / 2
        return ViewAnimationUtils.createCircularReveal(sheet, reveal.x, reveal.y, sr, er).apply {
            duration = longAnimDuration
            interpolator = AccelerateDecelerateInterpolator()
            doOnEnd {
                sheet.isInvisible = true
            }
        }
    }

    private fun makeInfoSheetRevealShowAnimation(): Animator {
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

    private fun getPreviewColor(): Int {
        return binding.preview.cardBackgroundColor.defaultColor
    }

    private fun setPreviewColor(@ColorInt color: Int) {
        binding.preview.setCardBackgroundColor(color)
    }

    private fun getInfoSheetColor(): Int {
        return binding.infoSheet.backgroundTintList?.defaultColor ?: 0
    }

    private fun setInfoSheetColor(@ColorInt color: Int) {
        binding.infoSheet.backgroundTintList = ColorStateList.valueOf(color)
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
        hideSoftInput()
        animInfoSheetShowing(color.toColorInt())
    }

    override fun setSoftInputMode() {
        // https://yatmanwong.medium.com/android-how-to-pan-the-page-up-more-25fc5c542a97
    }

    companion object {

        // being created by NavHostFragment, thus no newInstance() method
    }
}