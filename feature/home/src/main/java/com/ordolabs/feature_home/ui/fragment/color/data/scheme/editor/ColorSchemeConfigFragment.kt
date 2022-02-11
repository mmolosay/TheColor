package com.ordolabs.feature_home.ui.fragment.color.data.scheme.editor

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
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeConfigViewModel
import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.model.color.data.ColorSchemeRequest
import com.ordolabs.thecolor.util.InflaterUtil.cloneInViewContext
import com.google.android.material.R as RMaterial

/**
 * Fragment that displays color scheme configuration options and passes them
 * to [ColorSchemeConfigViewModel], where they being stored and could be used by parent fragment.
 *
 * [ColorSchemeEditorFragment] can be parent of `this` fragment.
 */
class ColorSchemeConfigFragment : BaseFragment() {

    private val binding: ColorSchemeConfigFragmentBinding by viewBinding(CreateMethod.BIND)
    private val schemeConfigVM: ColorSchemeConfigViewModel by viewModels {
        featureHomeComponent.viewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater
            .cloneInViewContext(container)
            .inflate(R.layout.color_scheme_config_fragment, container, false)
    }

    override fun collectViewModelsData() {
        collectAppliedConfig()
    }

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
                onSchemeModeChipChecked(isChecked, mode)
            }
            group.addView(chip)
        }

        val mode = schemeConfigVM.mode
        (group.getChildAt(mode.ordinal) as Chip).isChecked = true
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

        val sampleCount = schemeConfigVM.sampleCount
        val position = counts.indexOf(sampleCount).takeUnless { it == -1 } ?: return
        (group.getChildAt(position) as Chip).isChecked = true
    }

    private fun onSchemeModeChipChecked(
        isChecked: Boolean,
        mode: ColorScheme.Mode
    ) {
        if (!isChecked) return // do nothing
        schemeConfigVM.mode = mode
    }

    private fun onSchemeSampleCountChipChecked(
        isChecked: Boolean,
        sampleCount: Int
    ) {
        if (!isChecked) return // do nothing
        schemeConfigVM.sampleCount = sampleCount
    }

    // region Populate

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

    // endregion

    private fun collectAppliedConfig() =
        schemeConfigVM.appliedConfig.collectOnLifecycle { resource ->
            resource.ifSuccess { config ->
                populateTitles(config)
            }
        }

    companion object {
        fun newInstance() =
            ColorSchemeConfigFragment()
    }
}