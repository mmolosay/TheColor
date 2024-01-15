package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.scheme.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import by.kirich1409.viewbindingdelegate.viewBinding
import io.github.mmolosay.thecolor.presentation.color.data.ColorScheme
import io.github.mmolosay.thecolor.presentation.color.data.ColorSchemeRequest
import io.github.mmolosay.thecolor.presentation.home.R
import io.github.mmolosay.thecolor.presentation.home.databinding.ColorSchemeEditorFragmentBinding
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.base.BaseColorDataFragment
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.base.ColorDataView
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.scheme.ColorSchemeFragment
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.scheme.config.ColorSchemeConfigFragment
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.scheme.config.ColorSchemeConfigParent
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.scheme.config.ColorSchemeConfigView
import io.github.mmolosay.thecolor.presentation.util.InflaterUtil.cloneInViewContext
import io.github.mmolosay.thecolor.presentation.util.ext.ancestorOf
import io.github.mmolosay.thecolor.presentation.util.ext.by
import io.github.mmolosay.thecolor.presentation.util.ext.mediumAnimDuration
import io.github.mmolosay.thecolor.presentation.util.ext.setFragmentOrGet
import io.github.mmolosay.thecolor.presentation.R as CommonR

/**
 * Editor fragment for color scheme. It can:
 *  1. configure color scheme via [ColorSchemeConfigFragment];
 *  2. dispatch the configuration to [ColorSchemeEditorParent];
 *  3. display scheme via [ColorSchemeFragment].
 */
class ColorSchemeEditorFragment :
    BaseColorDataFragment<ColorScheme>(),
    ColorSchemeEditorView,
    ColorSchemeConfigParent {

    private val binding by viewBinding(ColorSchemeEditorFragmentBinding::bind)

    private val parent: ColorSchemeEditorParent? by lazy { ancestorOf() }
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
        setColorSchemeConfigFragment()
    }

    private fun setColorShemeFragment() {
        val container = binding.schemeFragmentContainer
        this.schemeView = setFragmentOrGet(container.id) {
            ColorSchemeFragment.newInstance()
        }
    }

    private fun setColorSchemeConfigFragment() {
        val container = binding.configFragmentContainer
        this.configView = setFragmentOrGet(container.id) {
            ColorSchemeConfigFragment.newInstance()
        }
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
            parent?.dispatchColorSchemeConfig(applied)
        }

    // endregion

    // region Animate

    private fun animDispatchChangesBtn(show: Boolean) =
        binding.dispatchChangesBtn.apply {
            if (show == !isInvisible) return@apply // already in dest state
            val translation = resources.getDimension(CommonR.dimen.offset_12)
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

    // region ColorSchemeConfigView

    // delegations

    override val appliedConfig: ColorSchemeRequest.Config?
        get() = configView?.appliedConfig

    override fun applyCurrentConfig(): ColorSchemeRequest.Config? =
        configView?.applyCurrentConfig()

    // endregion

    // region ColorSchemeConfigParent

    override fun onCurrentConfigChanged(
        applied: ColorSchemeRequest.Config,
        current: ColorSchemeRequest.Config
    ) {
        val hasChanges = (current != applied)
        animDispatchChangesBtn(show = hasChanges)
    }

    // endregion

    companion object {
        fun newInstance() =
            ColorSchemeEditorFragment()
    }
}
