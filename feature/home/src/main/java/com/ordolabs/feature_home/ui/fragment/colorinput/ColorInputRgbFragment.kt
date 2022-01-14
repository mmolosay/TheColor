package com.ordolabs.feature_home.ui.fragment.colorinput

import androidx.core.widget.doOnTextChanged
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentColorInputRgbBinding
import com.ordolabs.thecolor.model.color.ColorRgb
import com.ordolabs.thecolor.ui.util.inputfilter.PreventingInputFilter
import com.ordolabs.thecolor.ui.util.inputfilter.RangeInputFilter
import com.ordolabs.thecolor.util.ext.addFilters
import com.ordolabs.thecolor.util.ext.getText
import com.ordolabs.thecolor.util.ext.getTextString
import com.ordolabs.thecolor.util.ext.setTextPreservingSelection
import com.ordolabs.thecolor.util.struct.Resource
import kotlinx.coroutines.flow.Flow

class ColorInputRgbFragment :
    BaseColorInputFragment<ColorRgb>(R.layout.fragment_color_input_rgb) {

    private val binding: FragmentColorInputRgbBinding by viewBinding()

    override fun setViews() {
        setComponentInputsFilters()
        setComponentRTextWatcher()
        setComponentGTextWatcher()
        setComponentBTextWatcher()
    }

    private fun setComponentInputsFilters() = binding.run {
        val range = RangeInputFilter(min = 0, max = 255)
        val preventing = PreventingInputFilter(preceding = "0", what = "0")
        inputRgbR.editText?.addFilters(range, preventing)
        inputRgbG.editText?.addFilters(range, preventing)
        inputRgbB.editText?.addFilters(range, preventing)
    }

    private fun setComponentRTextWatcher() =
        binding.inputRgbR.editText?.doOnTextChanged { _, _, _, _ ->
            validateOnInputChanges()
        }

    private fun setComponentGTextWatcher() =
        binding.inputRgbG.editText?.doOnTextChanged { _, _, _, _ ->
            validateOnInputChanges()
        }

    private fun setComponentBTextWatcher() =
        binding.inputRgbB.editText?.doOnTextChanged { _, _, _, _ ->
            validateOnInputChanges()
        }

    // region BaseColorInputFragment

    override fun assembleColor(): ColorRgb {
        val r = binding.inputRgbR.getTextString()?.toIntOrNull()
        val g = binding.inputRgbG.getTextString()?.toIntOrNull()
        val b = binding.inputRgbB.getTextString()?.toIntOrNull()
        return ColorRgb(r, g, b)
    }

    override fun populateViews(color: ColorRgb): Unit =
        binding.run {
            inputRgbR.editText?.setTextPreservingSelection(color.r.toString())
            inputRgbG.editText?.setTextPreservingSelection(color.g.toString())
            inputRgbB.editText?.setTextPreservingSelection(color.b.toString())
        }

    override fun clearViews(): Unit =
        binding.run {
            binding.inputRgbR.getText()?.clear()
            binding.inputRgbG.getText()?.clear()
            binding.inputRgbB.getText()?.clear()
        }

    override fun getColorInputFlow(): Flow<Resource<ColorRgb>> =
        colorInputVM.colorRgb

    // endregion

    companion object {

        fun newInstance() = ColorInputRgbFragment()
    }
}