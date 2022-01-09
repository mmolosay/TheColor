package com.ordolabs.feature_home.ui.fragment.colordata.page.base

import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorDataPageBinding
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.ColorDataViewModel
import com.ordolabs.thecolor.util.ext.setFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class BaseColorDataPage :
    BaseFragment(R.layout.color_data_page),
    IColorDataPage {

    private val binding: ColorDataPageBinding by viewBinding()
    private val colorDataVM: ColorDataViewModel by sharedViewModel()

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
                colorDataVM.changeDataPage(page)
            }
            button.text = getChangePageBtnText()
        }
}