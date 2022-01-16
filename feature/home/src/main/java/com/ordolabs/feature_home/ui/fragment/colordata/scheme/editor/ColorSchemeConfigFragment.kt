package com.ordolabs.feature_home.ui.fragment.colordata.scheme.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.chip.Chip
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorSchemeConfigFragmentBinding
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeConfigViewModel
import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.util.InflaterUtil.cloneInViewContext
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

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

    companion object {
        fun newInstance() =
            ColorSchemeConfigFragment()
    }
}