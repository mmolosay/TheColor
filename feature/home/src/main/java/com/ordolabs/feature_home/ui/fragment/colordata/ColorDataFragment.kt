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
import com.ordolabs.feature_home.viewmodel.ColorDataViewModel
import com.ordolabs.thecolor.util.ColorUtil
import com.ordolabs.thecolor.util.ColorUtil.isDark
import com.ordolabs.thecolor.util.ext.makeArgumentsKey
import com.ordolabs.thecolor.util.ext.showToast
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import com.ordolabs.thecolor.R as RApp

class ColorDataFragment :
    BaseFragment(),
    IColorThemed {

    private val binding: ColorDataFragmentBinding by viewBinding(CreateMethod.BIND)
    private val colorDataVM: ColorDataViewModel by sharedViewModel()

    // TODO: implement trivial no content view, if color is null somehow
    override var color: ColorUtil.Color? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArguments()
        fetchColorData()
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

    private fun fetchColorData() =
        color?.let { color ->
            colorDataVM.fetchColorData(color)
        }

    override fun collectViewModelsData() {
        collectColorDetails()
        collectColorScheme()
        collectChangePageCommand()
        collectCoroutineException()
    }

    override fun setViews() {
        setViewPager()
    }

    private fun setViewPager() =
        binding.pager.let { pager ->
            val adapter = ColorDataPagerAdapter(this)
            pager.adapter = adapter
        }

    private fun collectColorDetails() =
        colorDataVM.details.collectOnLifecycle { resource ->
            resource.ifSuccess {
                val position = ColorDataPagerAdapter.Page.DETAILS.ordinal
                binding.pager.adapter?.notifyItemChanged(position)
            }
        }

    private fun collectColorScheme() =
        colorDataVM.scheme.collectOnLifecycle { resource ->
            resource.ifSuccess {
                val position = ColorDataPagerAdapter.Page.SCHEME.ordinal
                binding.pager.adapter?.notifyItemChanged(position)
            }
        }

    private fun collectChangePageCommand() =
        colorDataVM.changePageCommand.collectOnLifecycle { resource ->
            resource.ifSuccess { page ->
                binding.pager.setCurrentItem(page.ordinal, /*smoothScroll*/ true)
            }
        }

    private fun collectCoroutineException() =
        colorDataVM.coroutineExceptionMessageRes.collectOnLifecycle { idres ->
            val text = Result.runCatching {
                getString(idres)
            }.getOrNull() ?: return@collectOnLifecycle
            showToast(text)
        }

    companion object {

        private val ARGUMENTS_KEY_COLOR =
            "ARGUMENTS_KEY_COLOR".makeArgumentsKey<ColorDataFragment>()

        fun newInstance(color: ColorUtil.Color?) =
            ColorDataFragment().apply {
                arguments = bundleOf(
                    ARGUMENTS_KEY_COLOR to color
                )
            }
    }
}