package com.ordolabs.feature_home.ui.fragment.home

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.doOnLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.HomeFragmentBinding
import com.ordolabs.feature_home.di.DaggerFeatureHomeComponent
import com.ordolabs.feature_home.di.FeatureHomeComponent
import com.ordolabs.feature_home.di.FeatureHomeComponentKeeper
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.ui.fragment.color.data.ColorDataPagerFragment
import com.ordolabs.feature_home.ui.fragment.color.data.details.ColorDetailsParent
import com.ordolabs.feature_home.ui.fragment.color.input.page.ColorInputParent
import com.ordolabs.feature_home.ui.fragment.color.input.pager.ColorInputPagerFragment
import com.ordolabs.feature_home.ui.fragment.color.input.pager.ColorInputPagerView
import com.ordolabs.feature_home.viewmodel.HomeViewModel
import com.ordolabs.feature_home.viewmodel.colorinput.ColorValidatorViewModel
import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.model.color.ColorPreview
import com.ordolabs.thecolor.model.color.ColorPrototype
import com.ordolabs.thecolor.model.color.toColorInt
import com.ordolabs.thecolor.util.AnimationUtils
import com.ordolabs.thecolor.util.ext.appComponent
import com.ordolabs.thecolor.util.ext.bindPropertyAnimator
import com.ordolabs.thecolor.util.ext.by
import com.ordolabs.thecolor.util.ext.createCircularRevealAnimation
import com.ordolabs.thecolor.util.ext.getBottomVisibleInParent
import com.ordolabs.thecolor.util.ext.getDistanceToViewInParent
import com.ordolabs.thecolor.util.ext.hideSoftInput
import com.ordolabs.thecolor.util.ext.hideSoftInputAndClearFocus
import com.ordolabs.thecolor.util.ext.longAnimDuration
import com.ordolabs.thecolor.util.ext.mediumAnimDuration
import com.ordolabs.thecolor.util.ext.propertyAnimator
import com.ordolabs.thecolor.util.ext.propertyAnimatorOrNull
import com.ordolabs.thecolor.util.ext.replaceFragment
import com.ordolabs.thecolor.util.ext.setFragmentOrGet
import com.ordolabs.thecolor.util.ext.shortAnimDuration
import com.ordolabs.thecolor.util.restoreNavigationBarColor
import com.ordolabs.thecolor.util.setNavigationBarColor
import com.ordolabs.thecolor.util.struct.AnimatorDestination
import android.graphics.Color as ColorAndroid
import com.google.android.material.R as RMaterial
import com.ordolabs.thecolor.R as RApp

// TODO: refactor to state machine
// TODO: now BLANK state is being set in xml; configure all states manually in code
class HomeFragment :
    BaseFragment(),
    FeatureHomeComponentKeeper,
    HomeView,
    ColorInputParent,
    ColorDetailsParent {

    override val featureHomeComponent: FeatureHomeComponent by lazy(::makeFeatureHomeComponent)

    private val binding: HomeFragmentBinding by viewBinding()
    private val homeVM: HomeViewModel by viewModels {
        featureHomeComponent.savedStateViewModelFactoryFactory.create(this, defaultArgs = null)
    }
    private val colorValidatorVM: ColorValidatorViewModel by viewModels {
        featureHomeComponent.viewModelFactory
    }

    private var inputPagerView: ColorInputPagerView? = null
    private val previewResizeDest = AnimatorDestination()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onStop() {
        super.onStop()
        hideSoftInputAndClearFocus()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.inputPagerView = null
    }

    // region Restore state

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        restoreState()
    }

    private fun restoreState() =
        when (homeVM.state) {
            HomeView.State.BLANK -> restoreBlankState()
            HomeView.State.PREVIEW -> restorePreviewState()
            HomeView.State.DATA -> restoreDataState()
        }

    private fun restoreBlankState() {
        // blank from xml layout by default; do nothing
    }

    private fun restorePreviewState() {
        val preview = homeVM.preview ?: return
        animPreviewResize(collapse = false)
        tintPreviewBackground(preview)
    }

    private fun restoreDataState() {
        val preview = homeVM.preview ?: return
        scalePreviewGroup(collapsed = false)
        tintPreviewBackground(preview)
        showDataWrapper(visible = true)
        tintDataWrapper(preview)
    }

    // endregion

    // region Set up

    override fun setUp() {
        featureHomeComponent // init
    }

    private fun makeFeatureHomeComponent(): FeatureHomeComponent =
        DaggerFeatureHomeComponent
            .builder()
            .appComponent(appComponent)
            .build()

    // endregion

    // region Collect ViewModels data

    override fun collectViewModelsData() {
        collectColorPreview()
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
    private fun onColorPreviewEmpty(previous: ColorPreview?) =
        view?.doOnLayout {
            when (homeVM.state) {
                HomeView.State.PREVIEW -> animPreviewResize(collapse = true)
                HomeView.State.DATA -> animColorDataCollapsingOnPreviewEmpty()
                else -> Unit
            }
            inputPagerView?.clearCurrentColor()
            homeVM.preview = null
            homeVM.state = HomeView.State.BLANK
        }

    private fun onColorPreviewSuccess(preview: ColorPreview) =
        view?.doOnLayout a@{
            val colorInt = preview.toColorInt()
            when (homeVM.state) {
                HomeView.State.BLANK -> {
                    animPreviewResize(collapse = false)
                    animPreviewColorChanging(colorInt)
                    homeVM.state = HomeView.State.PREVIEW
                }
                HomeView.State.PREVIEW -> {
                    if (homeVM.preview == preview) return@a // already set
                    animPreviewColorChanging(colorInt)
                }
                HomeView.State.DATA -> {
                    if (!preview.isUserInput) return@a // collapse only if user changed color manually
                    val dataBg = getDataWrapperTint()
                    if (colorInt != dataBg) {
                        animColorDataCollapsingOnPreviewSuccess()
                        homeVM.state = HomeView.State.PREVIEW
                    }
                }
            }
            inputPagerView?.updateCurrentColor(preview)
            homeVM.preview = preview
        }

    // endregion

    // region Set fragments

    override fun setFragments() {
        setColorInputFragment()
        setColorDataFragment()
    }

    private fun setColorInputFragment() {
        val container = binding.colorInputFragmentContainer
        this.inputPagerView = setFragmentOrGet(container.id) {
            ColorInputPagerFragment.newInstance()
        }
    }

    private fun setColorDataFragment() {
        val container = binding.colorDataFragmentContainer
        setFragmentOrGet(container.id) {
            ColorDataPagerFragment.newInstance(color = null)
        }
    }

    // endregion

    // region Set views

    override fun setViews() {
        setProcceedBtn()
    }

    private fun setProcceedBtn() =
        binding.run {
            procceedBtn.setOnClickListener l@{
                val color = homeVM.preview ?: return@l
                hideSoftInput()
                animColorDataExpanding(color)
                replaceColorDataFragment(color)
                homeVM.state = HomeView.State.DATA
            }
        }

    // endregion

    // region View utils

    private fun replaceColorDataFragment(color: Color) {
        val fragment = ColorDataPagerFragment.newInstance(color)
        replaceFragment(fragment, binding.colorDataFragmentContainer.id)
    }

    private fun showPreviewGroup(visible: Boolean) {
        binding.previewGroup.isInvisible = !visible
    }

    private fun scalePreviewGroup(collapsed: Boolean) {
        val value = 0f to 1f by collapsed
        val preview = binding.previewGroup
        preview.scaleX = value
        preview.scaleY = value
    }

    private fun tintPreviewBackground(color: Color) {
        val tint = ColorStateList.valueOf(color.toColorInt())
        binding.previewCurrent.backgroundTintList = tint
    }

    private fun showDataWrapper(visible: Boolean) {
        binding.colorDataWrapper.isInvisible = !visible
    }

    @ColorInt
    private fun getDataWrapperTint(): Int? {
        return binding.colorDataWrapper.backgroundTintList?.defaultColor
    }

    private fun tintDataWrapper(color: Color) =
        binding.colorDataWrapper.doOnLayout {
            binding.colorDataWrapper.backgroundTintList =
                ColorStateList.valueOf(color.toColorInt())
            activity?.setNavigationBarColor(color)
        }

    private fun clearDataWrapperTint() =
        binding.colorDataWrapper.doOnLayout {
            binding.colorDataWrapper.backgroundTintList =
                ColorStateList.valueOf(ColorAndroid.TRANSPARENT)
            activity?.restoreNavigationBarColor()
        }

    // endregion

    // region Animate

    private fun animColorDataExpanding(color: Color) {
        if (homeVM.state.isData()) return // already expanded
        binding.root.post { // when ^ infoFragmentContainer becomes visible
            binding.scrollview.isScrollable = true
            AnimatorSet().apply {
                playSequentially(
                    makePreviewFallingAnimation(),
                    makeColorDataRevealAnimation(hide = false).apply {
                        doOnStart {
                            tintDataWrapper(color)
                        }
                    }
                )
            }.start()
        }
    }

    private fun animColorDataCollapsingOnPreviewEmpty() {
        AnimatorSet().apply {
            playSequentially(
                makeColorDataCollapsingAnimation(),
                makePreviewResizingAnimation(collapse = true).apply {
                    doOnStart {
                        // if new preview appeared during animation
                        if (homeVM.preview != null) {
                            cancel()
                        }
                    }
                }
            )
        }.start()
    }

    private fun animColorDataCollapsingOnPreviewSuccess() {
        if (!homeVM.state.isData()) return // already collapsed
        makeColorDataCollapsingAnimation().start()
    }

    private fun animPreviewColorChanging(@ColorInt color: Int) =
        binding.previewGroup.doOnLayout {
            makePreviewColorChangingAnimation(color).start()
        }

    private fun animPreviewResize(collapse: Boolean) {
        if (collapse && binding.previewGroup.scaleX == 0f) return // already collapsed
        if (!collapse && binding.previewGroup.scaleX == 1f) return // already expanded
        if (collapse == previewResizeDest.isEnd) return // already running towards desired dest
        val animator = makePreviewResizingAnimation(collapse)
        if (animator.isStarted) {
            previewResizeDest.reverse()
            animator.reverse()
        } else {
            previewResizeDest.set(collapse)
            animator.start()
        }
    }

    // endregion

    // region Animation

    private fun makeColorDataCollapsingAnimation(): Animator =
        AnimatorSet().apply {
            playSequentially(
                makeScrollingToTopAnimation(),
                makeColorDataRevealAnimation(hide = true).apply {
                    doOnEnd {
                        clearDataWrapperTint()
                        binding.scrollview.run {
                            scrollTo(0, 0)
                            isScrollable = false
                        }
                    }
                },
                makePreviewRisingAnimation()
            )
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

    private fun makePreviewResizingAnimation(collapse: Boolean): ValueAnimator {
        val group = binding.previewGroup
        val from = group.scaleX
        val to = if (collapse) 0f else 1f
        val animator = ValueAnimator.ofFloat(from, to).apply {
            interpolator = FastOutSlowInInterpolator()
            duration = 300L
            addUpdateListener {
                group.scaleX = animatedValue as Float
                group.scaleY = animatedValue as Float
            }
            doOnStart {
                showPreviewGroup(visible = true)
            }
            doOnEnd {
                // expanded state could be achieved by reversing collapsing animation
                showPreviewGroup(visible = (group.scaleX != 0f))
                previewResizeDest.clear()
            }
        }
        return group.propertyAnimator(View.SCALE_X, animator)
    }

    private fun makePreviewColorChangingAnimation(@ColorInt color: Int): Animator {
        val group = binding.previewGroup
        val current = binding.previewCurrent
        val updated = binding.previewUpdated

        val property = AnimationUtils.CustomViewProperty.CIRCULAR_REVEAL
        val existed = updated.propertyAnimatorOrNull<Animator>(property)
        existed?.cancel() // will trigger onEnd as well

        val animator = updated.createCircularRevealAnimation().apply {
            duration = 0L to shortAnimDuration by !group.isVisible /* finish instantly,
             if parent view group is hidden */
            interpolator = FastOutSlowInInterpolator()
            doOnStart {
                showPreviewGroup(visible = true) // make parent viewgroup visible
                updated.isInvisible = false
                updated.backgroundTintList = ColorStateList.valueOf(color)
            }
            doOnEnd {
                current.backgroundTintList = ColorStateList.valueOf(color)
                updated.isInvisible = (existed != null) /* otherwise will hide view and animation
                 will be played on not visible view */
            }
        }
        // existed may was canceled, but not unbound from view, thus replace it with new one
        updated.bindPropertyAnimator(property, animator)
        return animator
    }

    private fun makePreviewTranslationAnimation(down: Boolean): Animator {
        val group = binding.previewGroup
        val translation = calcPreviewTranslation()
        val from = 0f to translation by down
        val to = translation to 0f by down
        return ObjectAnimator
            .ofFloat(group, View.TRANSLATION_Y, from, to)
            .apply {
                interpolator = AnticipateOvershootInterpolator()
                doOnStart {
                    group.isInvisible = false
                }
            }
    }

    @SuppressLint("PrivateResource")
    private fun makePreviewElevationAnimation(flatten: Boolean): Animator {
        val group = binding.previewGroup
        val elevation = resources.getDimension(RMaterial.dimen.m3_card_elevated_elevation)
        val animator = if (flatten) { // reverse() can't be used when is a part of AnimatorSet
            ValueAnimator.ofFloat(elevation, 0f)
        } else {
            ValueAnimator.ofFloat(0f, elevation)
        }
        return animator.apply {
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                group.elevation = animatedValue as Float
            }
        }
    }

    private fun makeColorDataRevealAnimation(hide: Boolean): Animator {
        val info = binding.colorDataWrapper
        val group = binding.previewGroup
        val center = calcColorDataRevealCenter()
        val sr = group.width.toFloat() / 2
        val er = AnimationUtils.getCircularRevealMaxRadius(info, center)
        return info.createCircularRevealAnimation(!hide, center.x, center.y, sr, er).apply {
            duration = longAnimDuration
            interpolator = AccelerateDecelerateInterpolator()
            doOnStart {
                showDataWrapper(visible = true)
            }
            doOnEnd {
                showDataWrapper(visible = !hide)
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

    private fun calcColorDataRevealCenter(): Point {
        val data = binding.colorDataWrapper
        val bottom = data.getBottomVisibleInParent(binding.root) ?: data.height
        val padding = resources.getDimensionPixelSize(RApp.dimen.offset_32)
        val previewRadius = binding.previewGroup.height / 2
        val x = data.width / 2
        val yApprox = bottom - padding - previewRadius
        val y = yApprox.coerceIn(0, data.height)
        return Point(x, y)
    }

    private fun calcPreviewTranslation(): Float {
        val preview = binding.previewGroup
        val data = binding.colorDataWrapper
        val distance = preview.getDistanceToViewInParent(data, view)?.y ?: 0
        val addend = calcColorDataRevealCenter().y
        val radius = preview.height / 2
        return distance.toFloat() + addend - radius
    }

    // endregion

    // region ColorInputParent

    override fun onInputChanged(input: ColorPrototype) {
        if (colorValidatorVM.isSameAsColorPreview(input)) return
        colorValidatorVM.validateColor(input)
    }

    // endregion

    // region ColorDetailsParent

    override fun onExactColorClick(exact: Color) {
        val preview = ColorPreview(exact, isUserInput = false)
        colorValidatorVM.updateColorPreview(preview)
        replaceColorDataFragment(exact)
    }

    // endregion

    companion object {

        // being created by NavHostFragment, thus no newInstance() method
    }
}