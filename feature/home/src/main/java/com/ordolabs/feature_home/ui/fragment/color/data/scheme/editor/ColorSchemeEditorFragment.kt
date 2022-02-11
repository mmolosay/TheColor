package com.ordolabs.feature_home.ui.fragment.color.data.scheme.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorSchemeEditorFragmentBinding
import com.ordolabs.feature_home.ui.fragment.color.data.base.BaseColorDataFragment
import com.ordolabs.feature_home.ui.fragment.color.data.base.IColorDataFragment
import com.ordolabs.feature_home.ui.fragment.color.data.scheme.ColorSchemeFragment
import com.ordolabs.feature_home.util.FeatureHomeUtil.featureHomeComponent
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeConfigViewModel
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeEditorViewModel
import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.util.InflaterUtil.cloneInViewContext
import com.ordolabs.thecolor.util.ext.by
import com.ordolabs.thecolor.util.ext.mediumAnimDuration
import com.ordolabs.thecolor.util.ext.setFragment
import com.ordolabs.thecolor.util.struct.getOrNull
import com.ordolabs.thecolor.R as RApp

/**
 * Editor fragment for color scheme. It can:
 *  1. configure color scheme via [ColorSchemeConfigFragment];
 *  2. dispatch the configuration to [ColorSchemeEditorViewModel];
 *  3. display scheme in [ColorSchemeFragment].
 */
class ColorSchemeEditorFragment :
    BaseColorDataFragment<ColorScheme>() {

    private val binding: ColorSchemeEditorFragmentBinding by viewBinding(CreateMethod.BIND)
    private val schemeEditorVM: ColorSchemeEditorViewModel by viewModels {
        featureHomeComponent.viewModelFactory
    }
    private val schemeConfigVM: ColorSchemeConfigViewModel by viewModels {
        featureHomeComponent.viewModelFactory
    }

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
        val defaultConfig = schemeConfigVM.appliedConfig.value.getOrNull()!!
        schemeEditorVM.dispatchConfig(defaultConfig)
    }

    override fun collectViewModelsData() {
        collectHasConfigChangesCommand()
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
            val options = schemeConfigVM.applyConfig()
            schemeEditorVM.dispatchConfig(options)
        }

    private fun animDispatchChangesBtn(show: Boolean) =
        binding.dispatchChangesBtn.apply {
            if (show == !isInvisible) return@apply // already in dest state
            val translation = resources.getDimension(RApp.dimen.offset_12)
            val alphaValues = 1f to 0f
            val translationValues = 0f to translation
            alpha = alphaValues by !show // initial
            translationX = translationValues by !show // initial
            animate()
                .alpha(alphaValues by show)
                .translationX(translationValues by show)
                .setDuration(mediumAnimDuration)
                .setInterpolator(FastOutSlowInInterpolator())
                .withStartAction {
                    isVisible = true
                }
                .withEndAction {
                    isInvisible = !show
                }
                .start()
        }

    // region IColorDataFragment

    // delegate
    override fun populateViews(data: ColorScheme) {
        schemeFragment?.populateViews(data)
    }

    // endregion

    private fun collectHasConfigChangesCommand() =
        schemeConfigVM.hasChangesCommand.collectOnLifecycle { resource ->
            resource.ifSuccess { hasChanges ->
                animDispatchChangesBtn(show = hasChanges)
            }
        }

    companion object {
        fun newInstance() =
            ColorSchemeEditorFragment()
    }
}