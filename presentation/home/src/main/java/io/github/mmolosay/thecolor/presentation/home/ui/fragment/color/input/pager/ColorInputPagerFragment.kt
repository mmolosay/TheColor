package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.input.pager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import io.github.mmolosay.thecolor.presentation.color.Color
import io.github.mmolosay.thecolor.presentation.color.ColorPrototype
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.fragment.BaseFragment
import io.github.mmolosay.thecolor.presentation.home.input.ColorInput
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputViewData
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputViewModel
import io.github.mmolosay.thecolor.presentation.home.input.hex.ColorInputHexViewData
import io.github.mmolosay.thecolor.presentation.home.input.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.presentation.home.input.rgb.ColorInputRgbViewData
import io.github.mmolosay.thecolor.presentation.home.input.rgb.ColorInputRgbViewModel
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.input.page.ColorInputParent

/**
 * `Fragment` with `ViewPager` which contains `Fragment`s of specific color scheme inputs.
 * Requires parent `Fragment` to be an instance of [ColorInputParent].
 */
@AndroidEntryPoint
class ColorInputPagerFragment :
    BaseFragment(),
    ColorInputPagerView,
    ColorInputParent {

//    private val binding by viewBinding(ColorInputPagerFragmentBinding::bind)
//    private val colorInputVM: ColorInputViewModel by viewModels()
//
//    private val parent: ColorInputParent? by lazy { requireParentOf() }

    private val inputViewModel: ColorInputViewModel by viewModels(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<ColorInputViewModel.Factory> { factory ->
                val viewData = ColorInputViewData(requireContext())
                factory.create(viewData)
            }
        }
    )

    private val hexViewModel: ColorInputHexViewModel by viewModels(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<ColorInputHexViewModel.Factory> { factory ->
                val viewData = ColorInputHexViewData(requireContext())
                factory.create(viewData)
            }
        }
    )

    private val rgbViewModel: ColorInputRgbViewModel by viewModels(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<ColorInputRgbViewModel.Factory> { factory ->
                val viewData = ColorInputRgbViewData(requireContext())
                factory.create(viewData)
            }
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(inflater.context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycle))
            setContent {
                TheColorTheme {
                    ColorInput(
                        vm = inputViewModel,
                        hexViewModel = hexViewModel,
                        rgbViewModel = rgbViewModel,
                    )
                }
            }
        }

    // region Set views

    override fun setViews() {
//        setViewPager()
//        setTabs()
    }

//    private fun setViewPager() = binding.run {
//        val adapter = ColorInputPagerAdapter(this@ColorInputPagerFragment)
//        val decoration = MarginDecoration.Horizontal(
//            resources,
//            CommonR.dimen.offset_content_horizontal
//        )
//        pager.adapter = adapter
//        pager.offscreenPageLimit = adapter.itemCount
//        pager.addItemDecoration(decoration)
//    }

//    private fun setTabs() = binding.run {
//        TabLayoutMediator(tabs, pager, ::configureInputTab).attach()
//    }

//    private fun configureInputTab(tab: TabLayout.Tab, position: Int) {
//        val page = getFromEnumOrNull<ColorInputPagerAdapter.Page>(position) ?: return
//        tab.setText(page.titleRes)
//    }

    // endregion

    // region ColorInputPagerView

    override fun updateCurrentColor(color: Color) {
//        colorInputVM.updateCurrentColor(color)
    }

    override fun clearCurrentColor() {
//        colorInputVM.clearColorInput()
    }

    // endregion

    // region ColorInputParent

    // delegates
    override fun onInputChanged(input: ColorPrototype) {
//        parent?.onInputChanged(input)
    }

    // endregion

    companion object {
        fun newInstance() = ColorInputPagerFragment()
    }
}