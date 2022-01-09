package com.ordolabs.feature_home.ui.fragment.colordata

import android.os.Bundle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorDataSchemeFragmentBinding
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.ColorDataViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

// TODO: fragment should only display data; make ColorDataFragment obtain all data and pass to there
class ColorDataSchemeFragment : BaseFragment(R.layout.color_data_scheme_fragment) {

    private val binding: ColorDataSchemeFragmentBinding by viewBinding()
    private val colorDataVM: ColorDataViewModel by sharedViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: fetch scheme
    }

    override fun collectViewModelsData() {
        // impl me
    }

    override fun setViews() {
        // impl me
    }

    companion object {
        fun newInstance() =
            ColorDataSchemeFragment()
    }
}