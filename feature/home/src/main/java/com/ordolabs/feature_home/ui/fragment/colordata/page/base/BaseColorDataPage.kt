package com.ordolabs.feature_home.ui.fragment.colordata.page.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorDataPageBinding
import com.ordolabs.feature_home.ui.adapter.pager.ColorDataPagerAdapter
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.ui.fragment.colordata.IColorThemed
import com.ordolabs.feature_home.viewmodel.ColorDataViewModel
import com.ordolabs.thecolor.util.ColorUtil
import com.ordolabs.thecolor.util.ColorUtil.isDark
import com.ordolabs.thecolor.util.ext.getFromEnumCoerced
import com.ordolabs.thecolor.util.ext.setFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import com.ordolabs.thecolor.R as RApp

abstract class BaseColorDataPage :
    BaseFragment(),
    IColorDataPage {

    private val binding: ColorDataPageBinding by viewBinding(CreateMethod.BIND)
    private val colorDataVM: ColorDataViewModel by sharedViewModel()

    val color: ColorUtil.Color?
        get() = (parentFragment as? IColorThemed)?.color

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /* ViewPager2 inflates its fragments without container,
        * thus parent view group theme can not be used :< */
        val themeOverlay = if (color?.isDark() == true) {
            RApp.style.ThemeOverlay_TheColor_Dark
        } else {
            RApp.style.ThemeOverlay_TheColor_Light
        }
        val themedContext = ContextThemeWrapper(context, themeOverlay)
        return inflater
            .cloneInContext(themedContext)
            .inflate(R.layout.color_data_page, container, false)
    }

    override fun collectViewModelsData() {
        // nothing is here
    }

    override fun setViews() {
        setFragment()
        setChangePageBtn()
    }

    private fun setFragment() {
        val fragment = getContentFragmentNewInstance()
        setFragment(fragment)
    }

    private fun setChangePageBtn() =
        binding.changePageBtn.let { button ->
            button.setOnClickListener {
                val dest = getFromEnumCoerced<ColorDataPagerAdapter.Page>(page.ordinal + 1)
                colorDataVM.changeDataPage(dest)
            }
            button.text = getChangePageBtnText()
        }

    companion object {

    }
}