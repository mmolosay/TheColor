package com.ordolabs.feature_home.ui.fragment.color.input

import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorInputRgbFragmentBinding
import com.ordolabs.thecolor.model.color.ColorPrototype
import com.ordolabs.thecolor.ui.util.inputfilter.PreventingInputFilter
import com.ordolabs.thecolor.ui.util.inputfilter.RangeInputFilter
import com.ordolabs.thecolor.util.struct.Resource
import kotlinx.coroutines.flow.Flow

class ColorInputRgbFragment :
    BaseColorInputFragment<ColorPrototype.Rgb>(R.layout.color_input_rgb_fragment) {

    private val binding: ColorInputRgbFragmentBinding by viewBinding()

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

    override fun assembleColor(): ColorPrototype.Rgb {
        val r = binding.inputRgbR.getTextString()?.toIntOrNull()
        val g = binding.inputRgbG.getTextString()?.toIntOrNull()
        val b = binding.inputRgbB.getTextString()?.toIntOrNull()
        return ColorPrototype.Rgb(r, g, b)
    }

    override fun populateViews(color: ColorPrototype.Rgb): Unit =
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

    override fun getColorInputFlow(): Flow<Resource<ColorPrototype.Rgb>> =
        colorInputVM.colorRgb

    // endregion

    companion object {

        fun newInstance() =
            ColorInputRgbFragment()
    }
}