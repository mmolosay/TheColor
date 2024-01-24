package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.page.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import io.github.mmolosay.thecolor.presentation.color.Color
import io.github.mmolosay.thecolor.presentation.color.isDark
import io.github.mmolosay.thecolor.presentation.fragment.BaseFragment
import io.github.mmolosay.thecolor.presentation.home.R
import io.github.mmolosay.thecolor.presentation.home.databinding.ColorDataPageFragmentBinding
import io.github.mmolosay.thecolor.presentation.home.ui.adapter.pager.ColorDataPagerAdapter
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.ColorThemedView
import io.github.mmolosay.thecolor.presentation.home.viewmodel.color.data.ColorDataViewModel
import io.github.mmolosay.thecolor.presentation.util.ext.getNextFor
import io.github.mmolosay.thecolor.presentation.util.ext.parentViewModels
import io.github.mmolosay.thecolor.presentation.util.ext.setFragmentOrGet
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

abstract class BaseColorDataPage :
    BaseFragment(),
    ColorThemedView {

    // region Abstract

    abstract val page: ColorDataPagerAdapter.Page

    abstract fun makeColorDataFragmentNewInstance(): Fragment
    abstract fun getChangePageBtnText(): String

    // endregion

    private val binding by viewBinding(ColorDataPageFragmentBinding::bind)
    private val colorDataVM: ColorDataViewModel by parentViewModels()

    override val color: Color?
        get() = (parentFragment as? ColorThemedView)?.color

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /* ViewPager2 inflates its fragments without container,
        * thus parent view group theme can not be used :< */
        val themeOverlay = if (color?.isDark() == true) {
            DesignR.style.ThemeOverlay_TheColor_Dark
        } else {
            DesignR.style.ThemeOverlay_TheColor_Light
        }
        val themedContext = ContextThemeWrapper(context, themeOverlay)
        return inflater
            .cloneInContext(themedContext)
            .inflate(R.layout.color_data_page_fragment, container, false)
    }

    override fun collectViewModelsData() {
        // nothing is here
    }

    override fun setViews() {
        setColorDataFragment()
        setChangePageBtn()
    }

    private fun setColorDataFragment() {
        setFragmentOrGet { makeColorDataFragmentNewInstance() }
    }

    private fun setChangePageBtn() =
        binding.changePageBtn.let { button ->
            button.setOnClickListener {
                val dest = getNextFor(page)
                colorDataVM.changeDataPage(dest)
            }
            button.text = getChangePageBtnText()
        }
}