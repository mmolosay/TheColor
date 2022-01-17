package com.ordolabs.feature_home.ui.fragment.colordata.scheme.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.chip.Chip
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorSchemeConfigFragmentBinding
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeConfigViewModel
import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.model.color.data.ColorSchemeRequest
import com.ordolabs.thecolor.util.InflaterUtil.cloneInViewContext
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import com.google.android.material.R as RMaterial

/**
 * Fragment that displays color scheme configuration options and passes them
 * to [ColorSchemeConfigViewModel], where they being stored and could be used by parent fragment.
 *
 * [ColorSchemeEditorFragment] can be parent of `this` fragment.
 */
class ColorSchemeConfigFragment : BaseFragment() {

    private val binding: ColorSchemeConfigFragmentBinding by viewBinding(CreateMethod.BIND)
    private val schemeConfigVM: ColorSchemeConfigViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater
            .cloneInViewContext(container)
            .inflate(R.layout.color_scheme_config_fragment, container, false)
    }

    override fun setUp() {
        super.setUp()
        val config = schemeConfigVM.appliedConfig
        populateTitles(config)
    }

    override fun collectViewModelsData() {
        // nothing is here
    }

    override fun setViews() {
        setSchemeModeChipGroup()
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

    private fun onSchemeModeChipChecked(
        isChecked: Boolean,
        mode: ColorScheme.Mode
    ) {
        if (!isChecked) return // do nothing
        schemeConfigVM.mode = mode
    }

    // region Populate

    private fun populateTitles(config: ColorSchemeRequest.Config) {
        val mode = enumValues<ColorScheme.Mode>()[config.modeOrdinal]
        val sampleCount = config.sampleCount
        populateModeTitle(mode)
        populateSampleCountTitle(sampleCount)
    }

    private fun populateModeTitle(mode: ColorScheme.Mode) {
        val titleView = binding.modeChipsTitle
        val label = resources.getString(mode.labelRes)
        val color = titleView.context
            .getColorStateList(RMaterial.color.material_on_background_emphasis_medium)
            .defaultColor
        val base = resources.getString(R.string.color_scheme_config_mode_title)
        val title = buildSpannedString {
            append(base)
            append(' ')
            color(color) {
                append(label)
            }
        }
        titleView.text = title
    }

    private fun populateSampleCountTitle(sampleCount: Int) {
        // TODO: implement
    }

    // endregion

    companion object {
        fun newInstance() =
            ColorSchemeConfigFragment()
    }
}