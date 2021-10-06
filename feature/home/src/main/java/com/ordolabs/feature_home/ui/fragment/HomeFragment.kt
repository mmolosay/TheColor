package com.ordolabs.feature_home.ui.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
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
import com.ordolabs.thecolor.util.ext.propertyAnimator
import com.ordolabs.thecolor.util.ext.replaceFragment
import com.ordolabs.thecolor.util.ext.setFragment
import com.ordolabs.thecolor.util.setNavigationBarsLight
import com.ordolabs.thecolor.util.struct.AnimatorDestination
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.core.context.loadKoinModules
import com.ordolabs.thecolor.R as RApp

class HomeFragment : BaseFragment(R.layout.fragment_home) {

    private val binding: FragmentHomeBinding by viewBinding()
    private val homeVM: HomeViewModel by sharedViewModel()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()

    private val previewResizeDest = AnimatorDestination()

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

    private fun getPreviewColor(): Int {
        return binding.preview.backgroundTintList?.defaultColor ?: 0
    }

    @ColorInt
    private fun getInfoBackgroundColor(): Int? {
        return binding.infoFragmentContainer.backgroundTintList?.defaultColor
    }

    private fun setInfoBackgroundColor(@ColorInt color: Int) {
        binding.infoFragmentContainer.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun toggleInfoFragmentVisibility(visible: Boolean) {
        binding.infoFragmentContainer.isVisible = visible
    }

    private fun animInfoSheetExpanding(color: ColorUtil.Color) {
        if (homeVM.isInfoSheetShown) return
        toggleInfoFragmentVisibility(visible = true)
        binding.root.post { // when ^ infoFragmentContainer becomes visible
            AnimatorSet().apply {
                playSequentially(
                    makePreviewFallingAnimation(),
                    makeInfoSheetRevealAnimation(hide = false).apply {
                        doOnStart {
                            setInfoBackgroundColor(color.toColorInt())
                            activity?.setNavigationBarsLight(light = !color.isDark())
                        }
                    }
                )
                doOnEnd {
                    homeVM.isInfoSheetShown = true
                }
            }.start()
        }
    }

    private fun animInfoSheetCollapsingOnPreviewEmpty() {
        AnimatorSet().apply {
            playSequentially(
                makeInfoSheetCollapsingAnimation(),
                makePreviewTogglingAnimation(collapse = true).apply {
                    doOnStart {
                        colorInputVM.colorPreview.value.ifSuccess {
                            cancel()
                        }
                    }
                }
            )
        }.start()
    }

    private fun animInfoSheetCollapsingOnPreviewSuccess() {
        if (!homeVM.isInfoSheetShown) return
        makeInfoSheetCollapsingAnimation().start()
    }

    private fun makeInfoSheetCollapsingAnimation(): Animator =
        AnimatorSet().apply {
            playSequentially(
                makeScrollingToTopAnimation(),
                makeInfoSheetRevealAnimation(hide = true).apply {
                    doOnEnd {
                        setInfoBackgroundColor(Color.TRANSPARENT)
                        activity?.setNavigationBarsLight(light = true)
                    }
                },
                makePreviewRisingAnimation()
            )
            doOnStart {
                homeVM.isInfoSheetShown = false
            }
        }

    private fun animPreviewColorChanging(@ColorInt color: Int) =
        binding.previewWrapper.doOnLayout {
            if (color == getPreviewColor()) return@doOnLayout
            makePreviewColorChangingAnimation(color).start()
        }

    private fun animPreviewResize(collapse: Boolean) {
        if (collapse == binding.previewWrapper.isInvisible) return // already in desired dest
        if (collapse == previewResizeDest.isEnd) return // already running towards desired dest
        val animator = makePreviewTogglingAnimation(collapse)
        if (animator.isStarted) {
            previewResizeDest.reverse()
            animator.reverse()
            Log.d("PREVIEW", "collapse $collapse, animation reverse")
        } else {
            previewResizeDest.set(collapse)
            animator.start()
            Log.d("PREVIEW", "collapse $collapse, animation start")
        }
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

    private fun makePreviewTogglingAnimation(collapse: Boolean): ValueAnimator {
        val wrapper = binding.previewWrapper
        val current = wrapper.scaleX
        val end = if (collapse) 0f else 1f
        val animator = ValueAnimator.ofFloat(current, end).apply {
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
                // expanded state could be achieved by reversing collapsing animation
                wrapper.isInvisible = (wrapper.scaleX == 0f)
                previewResizeDest.clear()
            }
        }
        return wrapper.propertyAnimator(View.SCALE_X, animator)
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
        val info = binding.infoFragmentContainer
        val translation = if (down) {
            val distance = preview.getDistanceToViewInParent(info, view)?.y ?: 0
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
        val info = binding.infoFragmentContainer
        val preview = binding.previewWrapper
        val center = makeInfoSheetRevealCenter()
        var sr = preview.width.toFloat() / 2
        var er = AnimationUtils.getCircularRevealMaxRadius(info, center)
        if (hide) er.let {
            er = sr
            sr = it
        }
        return info.createCircularRevealAnimation(center.x, center.y, sr, er).apply {
            duration = longAnimDuration
            interpolator = AccelerateDecelerateInterpolator()
            doOnStart {
                toggleInfoFragmentVisibility(visible = true)
            }
            doOnEnd {
                toggleInfoFragmentVisibility(visible = !hide)
            }
        }
    }

    private fun makeScrollingToTopAnimation(): Animator {
        val scroll = binding.scrollview
        return ObjectAnimator.ofInt(scroll, "scrollY", 0).apply {
            duration = mediumAnimDuration
            interpolator = FastOutSlowInInterpolator()
        }
    }

    private fun makeInfoSheetRevealCenter(): Point {
        val info = binding.infoFragmentContainer
        val bottom = info.getBottomVisibleInParent(view) ?: info.height
        val padding = resources.getDimensionPixelSize(RApp.dimen.offset_32)
        val navbarHeight = InsetsUtil.getNavigationBarHeight(context) ?: 0
        val x = info.width / 2
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
            if (homeVM.isInfoSheetShown) {
                animInfoSheetCollapsingOnPreviewEmpty()
            } else {
                animPreviewResize(collapse = true)
            }
        }
    }

    private fun onColorPreviewSuccess(color: ColorUtil.Color) {
        binding.previewWrapper.doOnLayout {
            val colorInt = color.toColorInt()
            if (getInfoBackgroundColor() != colorInt) {
                animInfoSheetCollapsingOnPreviewSuccess()
            }
            animPreviewColorChanging(colorInt)
            animPreviewResize(collapse = false)
        }
    }

    private fun collectProcceedCommand() =
        colorInputVM.procceedCommand.collectOnLifecycle { resource ->
            resource.ifSuccess { color ->
                hideSoftInput()
                animInfoSheetExpanding(color)
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