package com.ordolabs.feature_home.ui.fragment.color.data.scheme.config

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.chip.Chip
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorSchemeConfigFragmentBinding
import com.ordolabs.feature_home.ui.fragment.BaseFragment
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

    // TODO: implement custom property delegate "by ancestors()"?
    private val parent: ColorSchemeConfigParent? by lazy { ancestorOf() }
    private var mode: ColorScheme.Mode = ColorScheme.Mode.DEFAULT
    private var sampleCount: Int = ColorSchemeRequest.Config.SAMPLE_COUNT_DEFAULT

    override var appliedConfig: ColorSchemeRequest.Config = assembleCurrentConfig()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater
            .cloneInViewContext(container)
            .inflate(R.layout.color_scheme_config_fragment, container, false)
    }

    // region Set views

    override fun setViews() {
        setSchemeModeChipGroup()
        setSchemeSampleCountChipGroup()
        populateTitles(appliedConfig)
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

        val mode = this.mode
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

        val sampleCount = this.sampleCount
        val position = counts.indexOf(sampleCount).takeUnless { it == -1 } ?: return
        (group.getChildAt(position) as Chip).isChecked = true
    }

    private fun onSchemeModeChipChecked(
        isChecked: Boolean,
        mode: ColorScheme.Mode
    ) {
        if (!isChecked) return // do nothing
        this.mode = mode
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

    // region ColorSchemeConfigView

    override fun applyCurrentConfig(): ColorSchemeRequest.Config =
        assembleCurrentConfig().also { current ->
            this.appliedConfig = current
            populateTitles(current)
        }

    // endregion

    // delegate to parent
    private fun onCurrentConfigChanged() {
        val current = assembleCurrentConfig()
        parent?.onCurrentConfigChanged(current)
    }

    private fun assembleCurrentConfig() =
        ColorSchemeRequest.Config(
            modeOrdinal = mode.ordinal,
            sampleCount = sampleCount
        )

    companion object {
        fun newInstance() =
            ColorSchemeConfigFragment()
    }
}