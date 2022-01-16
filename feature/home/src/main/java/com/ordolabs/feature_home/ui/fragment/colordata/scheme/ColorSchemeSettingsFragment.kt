package com.ordolabs.feature_home.ui.fragment.colordata.scheme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.chip.Chip
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorSchemeSettingsFragmentBinding
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeSettingsViewModel
import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.util.InflaterUtil.cloneInViewContext
import com.ordolabs.thecolor.util.struct.getOrNull
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ColorSchemeSettingsFragment : BaseFragment() {

    private val binding: ColorSchemeSettingsFragmentBinding by viewBinding(CreateMethod.BIND)
    private val colorSchemeSettingsVM: ColorSchemeSettingsViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater
            .cloneInViewContext(container)
            .inflate(R.layout.color_scheme_settings_fragment, container, false)
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
        colorSchemeSettingsVM.schemeModeOrdinal.value.getOrNull()?.let { checkedModeOrdinal ->
            (group.getChildAt(checkedModeOrdinal) as Chip).isChecked = true
        }
    }

    private fun onSchemeModeChipChecked(
        isChecked: Boolean,
        mode: ColorScheme.Mode
    ) {
        if (!isChecked) return // do nothing
        colorSchemeSettingsVM.updateSchemeModeOrdinal(mode.ordinal)
    }

    companion object {
        fun newInstance() =
            ColorSchemeSettingsFragment()
    }
}