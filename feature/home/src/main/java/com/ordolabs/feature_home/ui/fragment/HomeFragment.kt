package com.ordolabs.feature_home.ui.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.HomeFragmentBinding
import com.ordolabs.feature_home.di.featureHomeModule
import com.ordolabs.feature_home.ui.fragment.color.data.ColorDataPagerFragment
import com.ordolabs.feature_home.ui.fragment.color.input.ColorInputPagerFragment
import com.ordolabs.feature_home.viewmodel.HomeViewModel
import com.ordolabs.feature_home.viewmodel.colordata.details.ColorDetailsViewModel
import com.ordolabs.feature_home.viewmodel.colorinput.ColorValidatorViewModel
import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.model.color.ColorPreview
import com.ordolabs.thecolor.model.color.toColorInt
import com.ordolabs.thecolor.util.AnimationUtils
import com.ordolabs.thecolor.util.ext.by
import com.ordolabs.thecolor.util.ext.hideSoftInput
import com.ordolabs.thecolor.util.ext.hideSoftInputAndClearFocus
import com.ordolabs.thecolor.util.ext.longAnimDuration
import com.ordolabs.thecolor.util.ext.mediumAnimDuration
import com.ordolabs.thecolor.util.ext.replaceFragment
import com.ordolabs.thecolor.util.ext.setFragment
import com.ordolabs.thecolor.util.ext.shortAnimDuration
import com.ordolabs.thecolor.util.restoreNavigationBarColor
import com.ordolabs.thecolor.util.setNavigationBarColor
import com.ordolabs.thecolor.util.struct.AnimatorDestination
import com.ordolabs.thecolor.util.struct.getOrNull
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.core.context.loadKoinModules
import android.graphics.Color as ColorAndroid
import com.google.android.material.R as RMaterial
import com.ordolabs.thecolor.R as RApp

class HomeFragment : BaseFragment(R.layout.home_fragment) {

    private val binding: HomeFragmentBinding by viewBinding()
    private val homeVM: HomeViewModel by sharedViewModel()
    private val colorValidatorVM: ColorValidatorViewModel by sharedViewModel()
    private val colorDetailsVM: ColorDetailsViewModel by sharedViewModel()

    private val previewResizeDest = AnimatorDestination()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadKoinModules(featureHomeModule)
    }

    override fun onStop() {
        super.onStop()
        hideSoftInputAndClearFocus()
    }

    override fun collectViewModelsData() {
        collectColorPreview()
        collectGetExactColorCommand()
    }

    override fun setViews() {
        setColorInputFragment()
        setColorDataFragment()
        setProcceedBtn()
        toggleDataWrapperVisibility(visible = false)
    }

    private fun setColorInputFragment() {
        val fragment = ColorInputPagerFragment.newInstance()
        setFragment(fragment, binding.colorInputFragmentContainer.id)
    }

    private fun setColorDataFragment() {
        val fragment = ColorDataPagerFragment.newInstance(color = null)
        setFragment(fragment, binding.colorDataFragmentContainer.id)
    }

    private fun replaceColorDataFragment(color: Color) {
        val fragment = ColorDataPagerFragment.newInstance(color)
        replaceFragment(fragment, binding.colorDataFragmentContainer.id)
    }

    private fun setProcceedBtn() = binding.run {
        procceedBtn.setOnClickListener l@{
            val color = colorValidatorVM.colorPreview.value.getOrNull() ?: return@l
            hideSoftInput()
            animInfoSheetExpanding(color)
            replaceColorDataFragment(color)
        }
    }

    @ColorInt
    private fun getDataWrapperBackgroundColor(): Int? {
        return binding.colorDataWrapper.backgroundTintList?.defaultColor
    }

    private fun tintDataWrapperBackground(color: Color) {
        binding.colorDataWrapper.backgroundTintList =
            ColorStateList.valueOf(color.toColorInt())
        activity?.setNavigationBarColor(color)
    }

    private fun clearDataWrapperBackground() {
        binding.colorDataWrapper.backgroundTintList =
            ColorStateList.valueOf(ColorAndroid.TRANSPARENT)
        activity?.restoreNavigationBarColor()
    }

    private fun toggleDataWrapperVisibility(visible: Boolean) {
        binding.colorDataWrapper.isInvisible = !visible
    }

    private fun animInfoSheetExpanding(color: Color) {
        if (homeVM.isColorDataShown) return
        binding.root.post { // when ^ infoFragmentContainer becomes visible
            binding.scrollview.isScrollable = true
            AnimatorSet().apply {
                playSequentially(
                    makePreviewFallingAnimation(),
                    makeInfoSheetRevealAnimation(hide = false).apply {
                        doOnStart {
                            tintDataWrapperBackground(color)
                        }
                    }
                )
                doOnEnd {
                    homeVM.isColorDataShown = true
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
                        colorValidatorVM.colorPreview.value.ifSuccess {
                            cancel()
                        }
                    }
                }
            )
        }.start()
    }

    private fun animColorDataCollapsingOnPreviewSuccess() {
        if (!homeVM.isColorDataShown) return
        makeInfoSheetCollapsingAnimation().start()
    }

    private fun makeInfoSheetCollapsingAnimation(): Animator =
        AnimatorSet().apply {
            playSequentially(
                makeScrollingToTopAnimation(),
                makeInfoSheetRevealAnimation(hide = true).apply {
                    doOnEnd {
                        clearDataWrapperBackground()
                        binding.scrollview.run {
                            scrollTo(0, 0)
                            isScrollable = false
                        }
                    }
                },
                makePreviewRisingAnimation()
            )
            doOnStart {
                homeVM.isColorDataShown = false
            }
        }

    private fun animPreviewColorChanging(@ColorInt color: Int) =
        binding.previewWrapper.doOnLayout {
            makePreviewColorChangingAnimation(color).start()
        }

    private fun animPreviewResize(collapse: Boolean) {
        if (collapse && binding.previewWrapper.scaleX == 0f) return // already collapsed
        if (!collapse && binding.previewWrapper.scaleX == 1f) return // already expanded
        if (collapse == previewResizeDest.isEnd) return // already running towards desired dest
        val animator = makePreviewTogglingAnimation(collapse)
        if (animator.isStarted) {
            previewResizeDest.reverse()
            animator.reverse()
        } else {
            previewResizeDest.set(collapse)
            animator.start()
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

        val property = AnimationUtils.CustomViewProperty.CIRCULAR_REVEAL
        val existed = updated.propertyAnimatorOrNull<Animator>(property)
        existed?.cancel() // will trigger onEnd as well

        val animator = updated.createCircularRevealAnimation().apply {
            duration = 0L to shortAnimDuration by !wrapper.isVisible /* finish instantly,
             if parent view group is hidden */
            interpolator = FastOutSlowInInterpolator()
            doOnStart {
                updated.backgroundTintList = ColorStateList.valueOf(color)
                updated.isInvisible = false
            }
            doOnEnd {
                preview.backgroundTintList = ColorStateList.valueOf(color)
                updated.isInvisible = (existed != null) /* otherwise will hide view and animation
                 will be played on not visible view */
            }
        }
        // existed may was canceled, but not unbound from view, thus replace it with new one
        updated.bindPropertyAnimator(property, animator)
        return animator
    }

    private fun makePreviewTranslationAnimation(down: Boolean): Animator {
        val preview = binding.previewWrapper
        val info = binding.colorDataWrapper
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
        val elevation = resources.getDimension(RMaterial.dimen.m3_card_elevated_elevation)
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
        val info = binding.colorDataWrapper
        val preview = binding.previewWrapper
        val center = makeInfoSheetRevealCenter()
        val sr = preview.width.toFloat() / 2
        val er = AnimationUtils.getCircularRevealMaxRadius(info, center)
        return info.createCircularRevealAnimation(!hide, center.x, center.y, sr, er).apply {
            duration = longAnimDuration
            interpolator = AccelerateDecelerateInterpolator()
            doOnStart {
                toggleDataWrapperVisibility(visible = true)
            }
            doOnEnd {
                toggleDataWrapperVisibility(visible = !hide)
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
        val info = binding.colorDataWrapper
        val bottom = info.getBottomVisibleInScrollParent(binding.root) ?: info.height
        val padding = resources.getDimensionPixelSize(RApp.dimen.offset_32)
        val previewRadius = binding.preview.height / 2
        val x = info.width / 2
        val yApprox = bottom - padding - previewRadius
        val y = yApprox.coerceIn(0, info.height)
        return Point(x, y)
    }

    private fun collectColorPreview() =
        colorValidatorVM.colorPreview.collectOnLifecycle { resource ->
            binding.procceedBtn.isEnabled = resource.isSuccess
            resource.fold(
                onEmpty = ::onColorPreviewEmpty,
                onSuccess = ::onColorPreviewSuccess
            )
        }

    @Suppress("UNUSED_PARAMETER")
    private fun onColorPreviewEmpty(previous: ColorPreview?) {
        binding.previewWrapper.doOnLayout {
            if (homeVM.isColorDataShown) {
                animInfoSheetCollapsingOnPreviewEmpty()
            } else {
                animPreviewResize(collapse = true)
            }
        }
    }

    private fun onColorPreviewSuccess(preview: ColorPreview) {
        binding.previewWrapper.doOnLayout {
            val colorInt = preview.toColorInt()
            if (preview.isUserInput) { // collapse only if user changed color manually
                val colorDataBg = getDataWrapperBackgroundColor()
                if (colorInt != colorDataBg) {
                    animColorDataCollapsingOnPreviewSuccess()
                }
            } else {
                tintDataWrapperBackground(preview)
            }
            animPreviewColorChanging(colorInt)
            animPreviewResize(collapse = false)
        }
    }

    private fun collectGetExactColorCommand() =
        colorDetailsVM.getExactColorCommand.collectOnLifecycle { resource ->
            resource.ifSuccess { exactColor ->
                val preview = ColorPreview(exactColor, isUserInput = false)
                colorValidatorVM.updateColorPreview(preview)
                replaceColorDataFragment(exactColor)
            }
        }

    companion object {

        // being created by NavHostFragment, thus no newInstance() method
    }
}