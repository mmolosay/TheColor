package io.github.mmolosay.thecolor.presentation.home.ui.fragment.home

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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.doOnLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import io.github.mmolosay.thecolor.presentation.center.ColorCenter
import io.github.mmolosay.thecolor.presentation.center.ColorCenterShape
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.color.Color
import io.github.mmolosay.thecolor.presentation.color.ColorPreview
import io.github.mmolosay.thecolor.presentation.color.ColorPrototype
import io.github.mmolosay.thecolor.presentation.color.toColorInt
import io.github.mmolosay.thecolor.presentation.design.ColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.ProvideColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.details.ColorDetails
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.fragment.BaseFragment
import io.github.mmolosay.thecolor.presentation.home.HomeViewModelNew
import io.github.mmolosay.thecolor.presentation.home.R
import io.github.mmolosay.thecolor.presentation.home.databinding.HomeFragmentBinding
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.ColorDataPagerFragment
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.details.ColorDetailsParent
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.input.page.ColorInputParent
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.input.pager.ColorInputPagerView
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModel
import io.github.mmolosay.thecolor.presentation.home.viewmodel.color.input.ColorValidatorViewModel
import io.github.mmolosay.thecolor.presentation.input.ColorInput
import io.github.mmolosay.thecolor.presentation.input.ColorInputViewModel
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbViewModel
import io.github.mmolosay.thecolor.presentation.preview.ColorPreview
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewViewModel
import io.github.mmolosay.thecolor.presentation.scheme.ColorScheme
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel
import io.github.mmolosay.thecolor.presentation.util.AnimationUtils
import io.github.mmolosay.thecolor.presentation.util.ext.bindPropertyAnimator
import io.github.mmolosay.thecolor.presentation.util.ext.by
import io.github.mmolosay.thecolor.presentation.util.ext.createCircularRevealAnimation
import io.github.mmolosay.thecolor.presentation.util.ext.getBottomVisibleInParent
import io.github.mmolosay.thecolor.presentation.util.ext.getDistanceToViewInParent
import io.github.mmolosay.thecolor.presentation.util.ext.hideSoftInput
import io.github.mmolosay.thecolor.presentation.util.ext.hideSoftInputAndClearFocus
import io.github.mmolosay.thecolor.presentation.util.ext.longAnimDuration
import io.github.mmolosay.thecolor.presentation.util.ext.mediumAnimDuration
import io.github.mmolosay.thecolor.presentation.util.ext.propertyAnimator
import io.github.mmolosay.thecolor.presentation.util.ext.propertyAnimatorOrNull
import io.github.mmolosay.thecolor.presentation.util.ext.replaceFragment
import io.github.mmolosay.thecolor.presentation.util.ext.shortAnimDuration
import io.github.mmolosay.thecolor.presentation.util.restoreNavigationBarColor
import io.github.mmolosay.thecolor.presentation.util.setNavigationBarColor
import io.github.mmolosay.thecolor.presentation.util.struct.AnimatorDestination
import android.graphics.Color as ColorAndroid
import androidx.compose.ui.graphics.Color as ComposeColor
import com.google.android.material.R as RMaterial
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

@AndroidEntryPoint
class HomeFragment :
    BaseFragment(),
    HomeView,
    ColorInputParent,
    ColorDetailsParent {

    private val binding by viewBinding(HomeFragmentBinding::bind)
    private val homeVM: HomeViewModel by viewModels()
    private val colorValidatorVM: ColorValidatorViewModel by viewModels()

    private var inputPagerView: ColorInputPagerView? = null
    private val previewResizeDest = AnimatorDestination()

    private val homeViewModelNew: HomeViewModelNew by viewModels()

    private val colorInputViewModel: ColorInputViewModel by viewModels()
    private val colorInputHexViewModel: ColorInputHexViewModel by viewModels()
    private val colorInputRgbViewModel: ColorInputRgbViewModel by viewModels()

    private val colorPreviewViewModel: ColorPreviewViewModel by viewModels()

    private val colorCenterViewModel: ColorCenterViewModel by viewModels()
    private val colorDetailsViewModel: ColorDetailsViewModel by viewModels()
    private val colorSchemeViewModel: ColorSchemeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        this.state = createStateByType(homeVM.stateType)
        state.restoreState()
    }

    override fun onStop() {
        super.onStop()
        hideSoftInputAndClearFocus()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.inputPagerView = null
    }

    // region Collect ViewModels data

    override fun collectViewModelsData() {
        collectColorPreview()
    }

    private fun collectColorPreview() =
        colorValidatorVM.colorPreview.collectOnLifecycle { resource ->
            binding.proceedBtn.isEnabled = resource.isSuccess
            resource.fold(
                onEmpty = ::onColorPreviewEmpty,
                onSuccess = ::onColorPreviewSuccess
            )
        }

    private fun onColorPreviewEmpty() =
        view?.doOnLayout {
            state.showBlank()
            inputPagerView?.clearCurrentColor()
            homeVM.preview = null
        }

    private fun onColorPreviewSuccess(preview: ColorPreview) =
        view?.doOnLayout a@{
            state.showPreview(preview)
            inputPagerView?.updateCurrentColor(preview)
            homeVM.preview = preview
        }

    // endregion

    // region Set views

    override fun setViews() {
        setColorInputView()
        setProceedBtn()
        setColorPreviewView()
        setColorCenterView()
    }

    private fun setColorInputView() {
//        val container = binding.colorInputFragmentContainer
//        this.inputPagerView = setFragmentOrGet(container.id) {
//            ColorInputPagerFragment.newInstance()
//        }
        binding.colorInputView.setContent {
            TheColorTheme {
                ColorInput(
                    vm = colorInputViewModel,
                    hexViewModel = colorInputHexViewModel,
                    rgbViewModel = colorInputRgbViewModel,
                )
            }
        }
    }

    private fun setProceedBtn() {
        binding.proceedBtn.setOnClickListener l@{
            homeViewModelNew.proceed()
            binding.colorCenterWrapper.isVisible = true

            val color = homeVM.preview ?: return@l
            state.showData(color)
        }

        homeViewModelNew.proceedActionAvailabilityFlow.collectOnLifecycle { available ->
            binding.proceedBtn.isEnabled = available
            if (!available) {
                binding.colorCenterWrapper.isVisible = false
            }
        }
    }

    private fun setColorPreviewView() {
        binding.colorPreview.setContent {
            TheColorTheme {
                ColorPreview(vm = colorPreviewViewModel)
            }
        }
    }

    private fun setColorCenterView() {
//        val container = binding.colorDataFragmentContainer
//        setFragmentOrGet(container.id) {
//            ColorDataPagerFragment.newInstance(color = null)
//        }
        binding.colorCenterView.setContent {
            TheColorTheme {
                // TODO: move ProvideColorsOnTintedSurface() to outside?
                val colors = rememberContentColors(useLight = true) // TODO: use real value
                ProvideColorsOnTintedSurface(colors) {
                    ColorCenter()
                }
            }
        }
    }

    @Composable
    private fun ColorCenter() {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    clip = true
                    shape = ColorCenterShape
                }
                .background(ComposeColor(0xFF_123456)) // TODO: use real color
        ) {
            ColorCenter(
                vm = colorCenterViewModel,
                details = { ColorDetails(vm = colorDetailsViewModel) },
                scheme = { ColorScheme(vm = colorSchemeViewModel) },
                modifier = Modifier.padding(top = 24.dp),
            )
        }
    }

    @Composable
    private fun rememberContentColors(useLight: Boolean): ColorsOnTintedSurface =
        remember(useLight) {
            if (useLight) colorsOnDarkSurface() else colorsOnLightSurface()
        }

    // endregion

    // region View utils

    private fun replaceColorDataFragment(color: Color) {
        val fragment = ColorDataPagerFragment.newInstance(color)
        replaceFragment(fragment, binding.colorCenterView.id)
    }

    private fun showPreviewGroup(visible: Boolean) {
        binding.previewGroup.isInvisible = !visible
    }

    private fun scalePreviewGroup(collapse: Boolean) {
        val value = 0f to 1f by collapse
        val preview = binding.previewGroup
        preview.scaleX = value
        preview.scaleY = value
    }

    private fun tintPreview(color: Color) {
        val tint = ColorStateList.valueOf(color.toColorInt())
        binding.previewCurrent.backgroundTintList = tint
    }

    private fun showDataWrapper(visible: Boolean) {
        binding.colorCenterWrapper.isInvisible = !visible
    }

    @ColorInt
    private fun getDataWrapperTint(): Int? {
        return binding.colorCenterWrapper.backgroundTintList?.defaultColor
    }

    private fun tintDataWrapper(color: Color) =
        binding.colorCenterWrapper.doOnLayout {
            binding.colorCenterWrapper.backgroundTintList =
                ColorStateList.valueOf(color.toColorInt())
            activity?.setNavigationBarColor(color)
        }

    private fun clearDataWrapperTint() =
        binding.colorCenterWrapper.doOnLayout {
            binding.colorCenterWrapper.backgroundTintList =
                ColorStateList.valueOf(ColorAndroid.TRANSPARENT)
            activity?.restoreNavigationBarColor()
        }

    // endregion

    // region Animate

    private fun animColorDataExpanding(color: Color) {
        if (homeVM.stateType == HomeView.State.Type.DATA) return // already expanded
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
        if (homeVM.stateType != HomeView.State.Type.DATA) return // already collapsed
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
        val wrapper = binding.colorCenterWrapper
        val preview = binding.previewGroup
        val center = calcColorDataRevealCenter()
        val sr = preview.width.toFloat() / 2
        val er = AnimationUtils.getCircularRevealMaxRadius(wrapper, center)
        return wrapper.createCircularRevealAnimation(!hide, center.x, center.y, sr, er).apply {
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
        val wrapper = binding.colorCenterWrapper
        val bottom = wrapper.getBottomVisibleInParent(binding.root) ?: wrapper.height
        val padding = resources.getDimensionPixelSize(DesignR.dimen.offset_32)
        val previewRadius = binding.previewGroup.height / 2
        val x = wrapper.width / 2
        val yApprox = bottom - padding - previewRadius
        val y = yApprox.coerceIn(0, wrapper.height)
        return Point(x, y)
    }

    private fun calcPreviewTranslation(): Float {
        val preview = binding.previewGroup
        val wrapper = binding.colorCenterWrapper
        val distance = preview.getDistanceToViewInParent(wrapper, this.view)?.y ?: 0
        val addend = calcColorDataRevealCenter().y
        val radius = preview.height / 2
        return distance.toFloat() + addend - radius
    }

    // endregion

    // region HomeView

    override var state: HomeView.State = BlankState()

    override fun changeState(type: HomeView.State.Type) {
        this.state = createStateByType(type)
        homeVM.stateType = type
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

    // region States

    private fun createStateByType(type: HomeView.State.Type): HomeView.State =
        when (type) {
            HomeView.State.Type.BLANK -> BlankState()
            HomeView.State.Type.PREVIEW -> PreviewState()
            HomeView.State.Type.DATA -> DataState()
        }

    private inner class BlankState : HomeView.State(this) {

        override fun restoreState() {
            showPreviewGroup(visible = false)
            scalePreviewGroup(collapse = true)
            showDataWrapper(visible = false)
        }

        override fun showBlank() {
            // already in blank state; do nothing
        }

        override fun showPreview(preview: ColorPreview) {
            val color = preview.toColorInt()
            animPreviewResize(collapse = false)
            animPreviewColorChanging(color)
            view.changeState(Type.PREVIEW)
        }

        override fun showData(color: Color) {
            // illegal; do nothing
        }
    }

    private inner class PreviewState : HomeView.State(this) {

        override fun restoreState() {
            val preview = homeVM.preview ?: return
            showPreviewGroup(visible = true)
            scalePreviewGroup(collapse = false)
            tintPreview(preview)
            showDataWrapper(visible = false)
        }

        override fun showBlank() {
            animPreviewResize(collapse = true)
            view.changeState(Type.BLANK)
        }

        override fun showPreview(preview: ColorPreview) {
            if (homeVM.preview == preview) return // already set
            val color = preview.toColorInt()
            animPreviewColorChanging(color)
        }

        override fun showData(color: Color) {
            hideSoftInput()
            animColorDataExpanding(color)
//            replaceColorDataFragment(color)
            view.changeState(Type.DATA)
        }
    }

    private inner class DataState : HomeView.State(this) {

        override fun restoreState() {
            val preview = homeVM.preview ?: return
            showPreviewGroup(visible = false)
            scalePreviewGroup(collapse = false)
            tintPreview(preview)
            showDataWrapper(visible = true)
            tintDataWrapper(preview)
        }

        override fun showBlank() {
            animColorDataCollapsingOnPreviewEmpty()
            view.changeState(Type.BLANK)
        }

        override fun showPreview(preview: ColorPreview) {
            if (!preview.isUserInput) return // collapse only if user changed color manually
            val color = preview.toColorInt()
            val dataBg = getDataWrapperTint()
            if (dataBg == color) return // TODO: is ever a case?; debug
            animColorDataCollapsingOnPreviewSuccess()
            view.changeState(Type.PREVIEW)
        }

        override fun showData(color: Color) {
            // already showing data; do nothing
        }
    }

    // endregion

    companion object {

        // being created by NavHostFragment, thus no newInstance() method
    }
}