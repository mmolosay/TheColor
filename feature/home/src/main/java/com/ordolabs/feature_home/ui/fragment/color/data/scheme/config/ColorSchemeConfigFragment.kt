package com.ordolabs.feature_home.ui.fragment.color.data.scheme.config

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.chip.Chip
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorSchemeConfigFragmentBinding
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.util.FeatureHomeUtil.featureHomeComponent
import com.ordolabs.feature_home.viewmodel.color.data.scheme.ColorSchemeConfigViewModel
import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.model.color.data.ColorSchemeRequest
import com.ordolabs.thecolor.util.InflaterUtil.cloneInViewContext
import com.ordolabs.thecolor.util.ext.ancestorOf
import com.google.android.material.R as RMaterial

/**
 * Fragment that displays color scheme configuration options and provides UI to change them.
 */
class ColorSchemeConfigFragment :
    BaseFragment(),
    ColorSchemeConfigView {

    private val binding: ColorSchemeConfigFragmentBinding by viewBinding(CreateMethod.BIND)
    private val configVM: ColorSchemeConfigViewModel by viewModels {
        featureHomeComponent.savedStateViewModelFactoryFactory.create(this, defaultArgs = null)
    }

    // TODO: implement custom property delegate "by ancestors()"?
    private val parent: ColorSchemeConfigParent? by lazy { ancestorOf() }
    private var modeOrdinal: Int? = null
    private var sampleCount: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater
            .cloneInViewContext(container)
            .inflate(R.layout.color_scheme_config_fragment, container, false)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        populateCurrentConfig(configVM.currentConfig)
        populateAppliedConfig(configVM.appliedConfig)
    }

    // region Set up

    override fun setUp() {
        val current = configVM.currentConfig
        this.modeOrdinal = current.modeOrdinal
        this.sampleCount = current.sampleCount
    }

    // endregion

    // region Set views

    override fun setViews() {
        setSchemeModeChipGroup()
        setSchemeSampleCountChipGroup()
    }

    private fun setSchemeModeChipGroup() {
        val group = binding.modeChips
        val layout = R.layout.color_scheme_mode_chip
        val inflater = layoutInflater.cloneInViewContext(group)
        enumValues<ColorScheme.Mode>().forEach { mode ->
            val chip = inflater.inflate(layout, group, false) as Chip
            chip.setText(mode.labelRes)
            chip.setOnCheckedChangeListener { _, isChecked ->
                onSchemeModeChipChecked(isChecked, mode.ordinal)
            }
            group.addView(chip)
        }
    }

    private fun setSchemeSampleCountChipGroup() {
        val group = binding.sampleCountChips
        val layout = R.layout.color_scheme_mode_chip
        val inflater = layoutInflater.cloneInViewContext(group)
        val counts = ColorSchemeRequest.Config.sampleCounts
        counts.forEach { count ->
            val chip = inflater.inflate(layout, group, false) as Chip
            chip.text = count.toString()
            chip.setOnCheckedChangeListener { _, isChecked ->
                onSchemeSampleCountChipChecked(isChecked, count)
            }
            group.addView(chip)
        }
    }

    private fun onSchemeModeChipChecked(
        isChecked: Boolean,
        modeOrdinal: Int
    ) {
        if (!isChecked) return // do nothing
        this.modeOrdinal = modeOrdinal
        onCurrentConfigChanged()
    }

    private fun onSchemeSampleCountChipChecked(
        isChecked: Boolean,
        sampleCount: Int
    ) {
        if (!isChecked) return // do nothing
        this.sampleCount = sampleCount
        onCurrentConfigChanged()
    }

    // endregion

    // region Populate

    private fun populateAppliedConfig(config: ColorSchemeRequest.Config) {
        populateTitles(config)
    }

    private fun populateCurrentConfig(config: ColorSchemeRequest.Config) {
        populateMode(config.modeOrdinal)
        populateSampleCount(config.sampleCount)
    }

    private fun populateTitles(config: ColorSchemeRequest.Config) {
        val mode = enumValues<ColorScheme.Mode>()[config.modeOrdinal]
        val sampleCount = config.sampleCount
        populateTitle(
            titleView = binding.modeChipsTitle,
            baseRes = R.string.color_scheme_config_mode_title,
            variable = resources.getString(mode.labelRes)
        )
        populateTitle(
            titleView = binding.sampleCountTitle,
            baseRes = R.string.color_scheme_config_sample_count_title,
            variable = sampleCount.toString()
        )
    }

    private fun populateTitle(
        titleView: TextView,
        @StringRes baseRes: Int,
        variable: String
    ) {
        val color = titleView.context
            .getColorStateList(RMaterial.color.material_on_background_emphasis_medium)
            .defaultColor
        val base = resources.getString(baseRes)
        val title = buildSpannedString {
            append(base)
            append(' ')
            color(color) {
                append(variable)
            }
        }
        titleView.text = title
    }

    private fun populateMode(modeOrdinal: Int) {
        val chip = binding.modeChips.getChildAt(modeOrdinal) as Chip
        chip.isChecked = true
    }

    private fun populateSampleCount(sampleCount: Int) {
        val counts = ColorSchemeRequest.Config.sampleCounts
        val position = counts.indexOf(sampleCount).takeUnless { it == -1 } ?: return
        val chip = binding.sampleCountChips.getChildAt(position) as Chip
        chip.isChecked = true
    }

    // endregion

    // region ColorSchemeConfigView

    override val appliedConfig: ColorSchemeRequest.Config
        get() = configVM.appliedConfig

    override fun applyCurrentConfig(): ColorSchemeRequest.Config =
        assembleCurrentConfig()?.also { current ->
            configVM.appliedConfig = current
            populateAppliedConfig(current)
        } ?: configVM.appliedConfig

    // endregion

    // delegate to parent
    private fun onCurrentConfigChanged() {
        val applied = configVM.appliedConfig
        val current = assembleCurrentConfig()?.also { config ->
            configVM.currentConfig = config
        } ?: configVM.currentConfig
        parent?.onCurrentConfigChanged(applied, current)
    }

    private fun assembleCurrentConfig(): ColorSchemeRequest.Config? {
        val modeOrdinal = modeOrdinal ?: return null
        val sampleCount = sampleCount ?: return null
        return ColorSchemeRequest.Config(modeOrdinal, sampleCount)
    }

    companion object {
        fun newInstance() =
            ColorSchemeConfigFragment()
    }
}