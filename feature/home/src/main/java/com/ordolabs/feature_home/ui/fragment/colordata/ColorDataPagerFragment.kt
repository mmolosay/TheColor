package com.ordolabs.feature_home.ui.fragment.colordata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorDataFragmentBinding
import com.ordolabs.feature_home.ui.adapter.pager.ColorDataPagerAdapter
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.colordata.ColorDataViewModel
import com.ordolabs.thecolor.util.ColorUtil
import com.ordolabs.thecolor.util.ColorUtil.isDark
import com.ordolabs.thecolor.util.ext.makeArgumentsKey
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import com.ordolabs.thecolor.R as RApp

class ColorDataPagerFragment :
    BaseFragment(),
    IColorThemed {

    private val binding: ColorDataFragmentBinding by viewBinding(CreateMethod.BIND)
    private val colorDataVM: ColorDataViewModel by sharedViewModel()

    override var color: ColorUtil.Color? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArguments()
    }

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
            .inflate(R.layout.color_data_fragment, container, false)
    }

    private fun parseArguments() {
        parseColorArg()
    }

    private fun parseColorArg() {
        val key = ARGUMENTS_KEY_COLOR
        val args = arguments ?: return
        if (!args.containsKey(key)) return
        this.color = args.getParcelable(key)
    }

    override fun collectViewModelsData() {
        collectChangePageCommand()
    }

    override fun setViews() {
        setViewPager()
    }

    private fun setViewPager() =
        binding.pager.let { pager ->
            val adapter = ColorDataPagerAdapter(this)
            pager.adapter = adapter
        }

    private fun collectChangePageCommand() =
        colorDataVM.changePageCommand.collectOnLifecycle { resource ->
            resource.ifSuccess { page ->
                binding.pager.setCurrentItem(page.ordinal, /*smoothScroll*/ true)
            }
        }

    companion object {

        private val ARGUMENTS_KEY_COLOR =
            "ARGUMENTS_KEY_COLOR".makeArgumentsKey<ColorDataPagerFragment>()

        fun newInstance(color: ColorUtil.Color?) =
            ColorDataPagerFragment().apply {
                arguments = bundleOf(
                    ARGUMENTS_KEY_COLOR to color
                )
            }
    }
}