package com.ordolabs.feature_home.ui.fragment.color.data.scheme.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorSchemeEditorFragmentBinding
import com.ordolabs.feature_home.ui.fragment.color.data.base.BaseColorDataFragment
import com.ordolabs.feature_home.ui.fragment.color.data.base.ColorDataView
import com.ordolabs.feature_home.ui.fragment.color.data.scheme.ColorSchemeFragment
import com.ordolabs.feature_home.ui.fragment.color.data.scheme.config.ColorSchemeConfigFragment
import com.ordolabs.feature_home.ui.fragment.color.data.scheme.config.ColorSchemeConfigParent
import com.ordolabs.feature_home.ui.fragment.color.data.scheme.config.ColorSchemeConfigView
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeEditorViewModel
import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.model.color.data.ColorSchemeRequest
import com.ordolabs.thecolor.util.InflaterUtil.cloneInViewContext
import com.ordolabs.thecolor.util.ext.by
import com.ordolabs.thecolor.util.ext.mediumAnimDuration
import com.ordolabs.thecolor.util.ext.parentViewModels
import com.ordolabs.thecolor.util.ext.setFragment
import com.ordolabs.thecolor.R as RApp

/**
 * Editor fragment for color scheme. It can:
 *  1. configure color scheme via [ColorSchemeConfigFragment];
 *  2. dispatch the configuration to [ColorSchemeEditorViewModel];
 *  3. display scheme in [ColorSchemeFragment].
 */
class ColorSchemeEditorFragment :
    BaseColorDataFragment<ColorScheme>(),
    ColorSchemeConfigParent {

    private val binding: ColorSchemeEditorFragmentBinding by viewBinding(CreateMethod.BIND)
    private val schemeEditorVM: ColorSchemeEditorViewModel by parentViewModels()

    private var schemeView: ColorDataView<ColorScheme>? = null
    private var configView: ColorSchemeConfigView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater
            .cloneInViewContext(container)
            .inflate(R.layout.color_scheme_editor_fragment, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        this.schemeView = null
        this.configView = null
    }

    // region Set fragments

    override fun setFragments() {
        super.setFragments()
        setColorShemeFragment()
        setColorSchemeSettingsFragment()
    }

    private fun setColorShemeFragment() {
        val fragment = ColorSchemeFragment.newInstance()
        this.schemeView = fragment
        setFragment(fragment, binding.schemeFragmentContainer.id)
    }

    private fun setColorSchemeSettingsFragment() {
        val fragment = ColorSchemeConfigFragment.newInstance()
        this.configView = fragment.also { view ->
            val applied = view.appliedConfig
            schemeEditorVM.dispatchConfig(applied)
        }
        setFragment(fragment, binding.configFragmentContainer.id)
    }

    // endregion

    // region Set views

    override fun setViews() {
        setDispatchChangesBtn()
    }

    private fun setDispatchChangesBtn() =
        binding.dispatchChangesBtn.setOnClickListener l@{
            val applied = configView?.applyCurrentConfig() ?: return@l
            animDispatchChangesBtn(show = false)
            schemeEditorVM.dispatchConfig(applied)
        }

    // endregion

    // region Animate

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

    // endregion

    // region ColorDataView

    // delegate
    override fun populateViews(data: ColorScheme) {
        schemeView?.populateViews(data)
    }

    // endregion

    // region ColorSchemeConfigParent

    override fun onCurrentConfigChanged(current: ColorSchemeRequest.Config) {
        val applied = configView?.appliedConfig ?: return
        val hasChanges = (current != applied)
        animDispatchChangesBtn(show = hasChanges)
    }

    // endregion

    companion object {
        fun newInstance() =
            ColorSchemeEditorFragment()
    }
}