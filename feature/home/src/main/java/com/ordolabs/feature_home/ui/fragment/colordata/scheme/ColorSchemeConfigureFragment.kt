package com.ordolabs.feature_home.ui.fragment.colordata.scheme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorSchemeConfigureFragmentBinding
import com.ordolabs.feature_home.ui.fragment.colordata.base.BaseColorDataFragment
import com.ordolabs.feature_home.ui.fragment.colordata.base.IColorDataFragment
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeObtainViewModel
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeSettingsViewModel
import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.util.InflaterUtil.cloneInViewContext
import com.ordolabs.thecolor.util.ext.setFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ColorSchemeConfigureFragment :
    BaseColorDataFragment<ColorScheme>() {

    private val binding: ColorSchemeConfigureFragmentBinding by viewBinding(CreateMethod.BIND)
    private val colorSchemeObtainVM: ColorSchemeObtainViewModel by sharedViewModel()
    private val colorSchemeSettingsVM: ColorSchemeSettingsViewModel by sharedViewModel()

    private var schemeFragment: IColorDataFragment<ColorScheme>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.cloneInViewContext(container)
            .inflate(R.layout.color_scheme_configure_fragment, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        this.schemeFragment = null
    }

    override fun collectViewModelsData() {
        // nothing in here
    }

    override fun setViews() {
        setColorShemeFragment()
        setColorSchemeSettingsFragment()
        setFetchColorSchemeBtn()
    }

    private fun setColorShemeFragment() {
        val fragment = ColorSchemeFragment()
        this.schemeFragment = fragment
        setFragment(fragment, binding.colorSchemeFragmentContainer.id)
    }

    private fun setColorSchemeSettingsFragment() {
        val fragment = ColorSchemeSettingsFragment()
        setFragment(fragment, binding.colorSchemeSettingsFragmnetContainer.id)
    }

    private fun setFetchColorSchemeBtn() =
        binding.fetchColorSchemeBtn.setOnClickListener l@{
            val request = colorSchemeSettingsVM.assembleColorSchemeRequest()
            colorSchemeObtainVM.getColorScheme(request)
        }

    // region IColorDataFragment

    // delegate
    override fun populateViews(data: ColorScheme) {
        schemeFragment?.populateViews(data)
    }

    // endregion

    companion object {
        fun newInstance() =
            ColorSchemeConfigureFragment()
    }
}