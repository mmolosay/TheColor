package com.ordolabs.feature_home.ui.fragment.color.data

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorDataPagerFragmentBinding
import com.ordolabs.feature_home.ui.adapter.pager.ColorDataPagerAdapter
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.color.data.ColorDataViewModel
import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.model.color.isDark
import dagger.hilt.android.AndroidEntryPoint
import com.ordolabs.thecolor.R as RApp

@AndroidEntryPoint
class ColorDataPagerFragment :
    BaseFragment(),
    ColorThemedView {

    private val binding by viewBinding(ColorDataPagerFragmentBinding::bind)
    private val colorDataVM: ColorDataViewModel by viewModels()

    override var color: Color? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val themeOverlay = if (color?.isDark() == true) {
            RApp.style.ThemeOverlay_TheColor_Dark
        } else {
            RApp.style.ThemeOverlay_TheColor_Light
        }
        val themedContext = ContextThemeWrapper(context, themeOverlay)
        return inflater
            .cloneInContext(themedContext)
            .inflate(R.layout.color_data_pager_fragment, container, false)
    }

    // region Set up

    override fun setUp() {
        parseArguments()
    }

    private fun parseArguments() {
        val args = arguments ?: return
        parseColor(args)
    }

    private fun parseColor(args: Bundle) {
        val key = ARGUMENTS_KEY_COLOR
        if (!args.containsKey(key)) return
        this.color = args.getParcelable(key)
    }

    // endregion

    // region Collect ViewModels data

    override fun collectViewModelsData() {
        collectChangePageCommand()
    }

    private fun collectChangePageCommand() =
        colorDataVM.changePageCommand.collectOnLifecycle { resource ->
            resource.ifSuccess { page ->
                binding.pager.setCurrentItem(page.ordinal, /*smoothScroll*/ true)
            }
        }

    // endregion

    // region Set views

    override fun setViews() {
        setViewPager()
    }

    private fun setViewPager() =
        binding.pager.let { pager ->
            val adapter = ColorDataPagerAdapter(this)
            pager.adapter = adapter
            pager.offscreenPageLimit = adapter.itemCount
        }

    // endregion

    companion object {

        private const val ARGUMENTS_KEY_COLOR = "ARGUMENTS_KEY_COLOR"

        fun newInstance(color: Color?) =
            ColorDataPagerFragment().apply {
                arguments = bundleOf(
                    ARGUMENTS_KEY_COLOR to color
                )
            }
    }
}