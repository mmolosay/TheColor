package com.ordolabs.feature_home.ui.fragment.colordata.scheme.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorSchemeEditorFragmentBinding
import com.ordolabs.feature_home.ui.fragment.colordata.base.BaseColorDataFragment
import com.ordolabs.feature_home.ui.fragment.colordata.base.IColorDataFragment
import com.ordolabs.feature_home.ui.fragment.colordata.scheme.ColorSchemeFragment
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeConfigViewModel
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeEditorViewModel
import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.util.InflaterUtil.cloneInViewContext
import com.ordolabs.thecolor.util.ext.setFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * Editor fragment for color scheme. It can:
 *  1. configure color scheme via [ColorSchemeConfigFragment];
 *  2. dispatch the configuration to [ColorSchemeEditorViewModel];
 *  3. display scheme in [ColorSchemeFragment].
 */
class ColorSchemeEditorFragment :
    BaseColorDataFragment<ColorScheme>() {

    private val binding: ColorSchemeEditorFragmentBinding by viewBinding(CreateMethod.BIND)
    private val schemeEditorVM: ColorSchemeEditorViewModel by sharedViewModel()
    private val schemeConfigVM: ColorSchemeConfigViewModel by sharedViewModel()

    private var schemeFragment: IColorDataFragment<ColorScheme>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.cloneInViewContext(container)
            .inflate(R.layout.color_scheme_editor_fragment, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        this.schemeFragment = null
    }

    override fun setUp() {
        super.setUp()
        val defaultConfig = schemeConfigVM.assembleConfig()
        schemeEditorVM.dispatchConfig(defaultConfig)
    }

    override fun collectViewModelsData() {
        // nothing in here
    }

    override fun setViews() {
        setColorShemeFragment()
        setColorSchemeSettingsFragment()
        setDispatchChangesBtn()
    }

    private fun setColorShemeFragment() {
        val fragment = ColorSchemeFragment()
        this.schemeFragment = fragment
        setFragment(fragment, binding.colorSchemeFragmentContainer.id)
    }

    private fun setColorSchemeSettingsFragment() {
        val fragment = ColorSchemeConfigFragment()
        setFragment(fragment, binding.colorSchemeSettingsFragmnetContainer.id)
    }

    private fun setDispatchChangesBtn() =
        binding.dispatchChangesBtn.setOnClickListener l@{
            val options = schemeConfigVM.assembleConfig()
            schemeEditorVM.dispatchConfig(options)
        }

    // region IColorDataFragment

    // delegate
    override fun populateViews(data: ColorScheme) {
        schemeFragment?.populateViews(data)
    }

    // endregion

    companion object {
        fun newInstance() =
            ColorSchemeEditorFragment()
    }
}