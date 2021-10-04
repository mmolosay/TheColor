package com.ordolabs.feature_home.ui.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.doOnLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentHomeBinding
import com.ordolabs.feature_home.di.featureHomeModule
import com.ordolabs.feature_home.ui.fragment.colorinput.ColorInputHostFragment
import com.ordolabs.feature_home.viewmodel.ColorInputViewModel
import com.ordolabs.feature_home.viewmodel.HomeViewModel
import com.ordolabs.thecolor.util.AnimationUtils
import com.ordolabs.thecolor.util.ColorUtil
import com.ordolabs.thecolor.util.ColorUtil.isDark
import com.ordolabs.thecolor.util.ColorUtil.toColorInt
import com.ordolabs.thecolor.util.InsetsUtil
import com.ordolabs.thecolor.util.ext.createCircularRevealAnimation
import com.ordolabs.thecolor.util.ext.getBottomVisibleInParent
import com.ordolabs.thecolor.util.ext.getDistanceToViewInParent
import com.ordolabs.thecolor.util.ext.hideSoftInput
import com.ordolabs.thecolor.util.ext.longAnimDuration
import com.ordolabs.thecolor.util.ext.mediumAnimDuration
import com.ordolabs.thecolor.util.ext.replaceFragment
import com.ordolabs.thecolor.util.ext.setFragment
import com.ordolabs.thecolor.util.setNavigationBarsLight
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.core.context.loadKoinModules
import com.ordolabs.thecolor.R as RApp

class HomeFragment : BaseFragment(R.layout.fragment_home) {

    private val binding: FragmentHomeBinding by viewBinding()
    private val homeVM: HomeViewModel by sharedViewModel()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadKoinModules(featureHomeModule)
    }

    override fun onPause() {
        super.onPause()
        view?.clearFocus()
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

    private fun replaceColorInformationFragment() {
        val fragment = ColorInformationFragment.newInstance()
        replaceFragment(fragment, binding.infoFragmentContainer.id)
    }

    private fun updateColorPreview(@ColorInt color: Int) =
        binding.previewWrapper.doOnLayout {
            if (color == getPreviewColor()) return@doOnLayout
            makePreviewColorChangingAnimation(color).start()
        }

    private fun getPreviewColor(): Int {
        return binding.preview.backgroundTintList?.defaultColor ?: 0
    }

    @ColorInt
    private fun getInfoSheetColor(): Int? {
        return binding.infoSheet.backgroundTintList?.defaultColor
    }

    private fun setInfoSheetColor(@ColorInt color: Int) {
        binding.infoSheet.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun animInfoSheetShowing(color: ColorUtil.Color) {
        if (homeVM.isInfoSheetShown) return
        binding.infoWrapper.isVisible = true
        binding.root.post { // when ^ infoWrapper becomes visible
            val animatorSet = AnimatorSet()
            animatorSet
                .play(makePreviewFallingAnimation())
                .before(makeInfoSheetRevealAnimation(hide = false).apply {
                    doOnStart {
                        setInfoSheetColor(color.toColorInt())
                        activity?.setNavigationBarsLight(light = !color.isDark())
                    }
                })
            animatorSet.doOnEnd {
                homeVM.isInfoSheetShown = true
            }
            animatorSet.start()
        }
    }

    private fun animInfoSheetHiding() {
        if (!homeVM.isInfoSheetShown) return
        val animatorSet = AnimatorSet()
        animatorSet
            .play(makeInfoSheetRevealAnimation(hide = true).apply {
                doOnEnd {
                    activity?.setNavigationBarsLight(light = true)
                }
            })
            .before(makePreviewRisingAnimation())
        animatorSet.doOnStart {
            homeVM.isInfoSheetShown = false
        }
        animatorSet.start()
    }

    private fun animPreviewResize(collapse: Boolean) {
        if (collapse == !binding.previewWrapper.isVisible) return
        AnimatorSet().apply {
            playTogether(
                makePreviewTogglingAnimation(collapse)
            )
        }.start()
    }

    private fun makePreviewRisingAnimation() =
        AnimatorSet().apply {
            playTogether(
                makePreviewTranslationAnimation(down = false),
                makePreviewElevationAnimation(flatten = false)
            )
            duration = longAnimDuration
        }

    private fun makePreviewFallingAnimation() =
        AnimatorSet().apply {
            playTogether(
                makePreviewTranslationAnimation(down = true),
                makePreviewElevationAnimation(flatten = true)
            )
            duration = longAnimDuration
        }

    private fun makePreviewTogglingAnimation(collapse: Boolean): Animator {
        val wrapper = binding.previewWrapper
        val start = if (collapse) wrapper.scaleX else 0f
        val end = if (collapse) 0f else 1f
        return ValueAnimator.ofFloat(start, end).apply {
            interpolator = FastOutSlowInInterpolator()
            duration = 300L
            addUpdateListener {
                wrapper.scaleX = animatedValue as Float
                wrapper.scaleY = animatedValue as Float
            }
            doOnStart {
                wrapper.isInvisible = false
            }
            doOnEnd {
                wrapper.isInvisible = collapse
            }
        }
    }

    private fun makePreviewColorChangingAnimation(@ColorInt color: Int): Animator {
        val preview = binding.preview
        val updated = binding.previewUpdated
        val wrapper = binding.previewWrapper
        return updated.createCircularRevealAnimation().apply {
            duration = if (wrapper.isVisible) mediumAnimDuration else 0L
            interpolator = FastOutSlowInInterpolator()
            doOnStart {
                updated.backgroundTintList = ColorStateList.valueOf(color)
                updated.isInvisible = false
            }
            doOnEnd {
                preview.backgroundTintList = ColorStateList.valueOf(color)
                updated.isInvisible = true
            }
        }
    }

    private fun makePreviewTranslationAnimation(down: Boolean): Animator {
        val preview = binding.previewWrapper
        val translation = if (down) {
            val distance = preview.getDistanceToViewInParent(binding.infoSheet, view)?.y ?: 0
            val addend = makeInfoSheetRevealCenter().y
            val radius = preview.height / 2
            distance.toFloat() + addend - radius
        } else {
            0f
        }
        return ObjectAnimator
            .ofFloat(preview, View.TRANSLATION_Y, translation)
            .apply {
                interpolator = AnticipateOvershootInterpolator()
            }
    }

    private fun makePreviewElevationAnimation(flatten: Boolean): Animator {
        val preview = binding.previewWrapper
        val elevation = resources.getDimension(R.dimen.home_preview_elevation)
        val animator = if (flatten) { // reverse() can't be used when is a part of AnimatorSet
            ValueAnimator.ofFloat(elevation, 0f)
        } else {
            ValueAnimator.ofFloat(0f, elevation)
        }
        return animator.apply {
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                preview.elevation = animatedValue as Float
            }
        }
    }

    private fun makeInfoSheetRevealAnimation(hide: Boolean): Animator {
        val sheet = binding.infoSheet
        val preview = binding.previewWrapper
        val center = makeInfoSheetRevealCenter()
        var sr = preview.width.toFloat() / 2
        var er = AnimationUtils.getCircularRevealMaxRadius(sheet, center)
        if (hide) er.let {
            er = sr
            sr = it
        }
        return sheet.createCircularRevealAnimation(center.x, center.y, sr, er).apply {
            duration = longAnimDuration
            interpolator = AccelerateDecelerateInterpolator()
            doOnStart {
                sheet.isInvisible = false
            }
            doOnEnd {
                sheet.isInvisible = hide
            }
        }
    }

    private fun makeInfoSheetRevealCenter(): Point {
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
        binding.previewWrapper.doOnLayout {
            animInfoSheetHiding()
            Log.d("colorpreviewtoggling", "collapse: true; color: empty")
            animPreviewResize(collapse = true)
        }
    }

    private fun onColorPreviewSuccess(color: ColorUtil.Color) {
        binding.previewWrapper.doOnLayout {
            val colorInt = color.toColorInt()
            if (getInfoSheetColor() != colorInt) {
                animInfoSheetHiding()
            }
            updateColorPreview(colorInt)
            Log.d("colorpreviewtoggling", "collapse: false; color: ${color.hex}")
            animPreviewResize(collapse = false)
        }
    }

    private fun collectProcceedCommand() =
        colorInputVM.procceedCommand.collectOnLifecycle { resource ->
            resource.ifSuccess { color ->
                hideSoftInput()
                animInfoSheetShowing(color)
                replaceColorInformationFragment()
            }
        }

    override fun setSoftInputMode() {
        // https://yatmanwong.medium.com/android-how-to-pan-the-page-up-more-25fc5c542a97
    }

    companion object {

        // being created by NavHostFragment, thus no newInstance() method
    }
}