package com.ordolabs.feature_home.ui.fragment.colorinput

import androidx.core.widget.doOnTextChanged
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentColorInputRgbBinding
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.ColorInputViewModel
import com.ordolabs.thecolor.model.ColorRgbPresentation
import com.ordolabs.thecolor.ui.util.inputfilter.PreventingInputFilter
import com.ordolabs.thecolor.ui.util.inputfilter.RangeInputFilter
import com.ordolabs.thecolor.util.ext.addFilters
import com.ordolabs.thecolor.util.ext.getTextString
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ColorInputRgbFragment :
    BaseFragment(R.layout.fragment_color_input_rgb) {

    private val binding: FragmentColorInputRgbBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()

    private var isTypedByUser = true

    override fun collectViewModelsData() {
        collectColorRgb()
    }

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
            if (isTypedByUser) validateColorInput()
        }

    private fun setComponentGTextWatcher() =
        binding.inputRgbG.editText?.doOnTextChanged { _, _, _, _ ->
            if (isTypedByUser) validateColorInput()
        }

    private fun setComponentBTextWatcher() =
        binding.inputRgbB.editText?.doOnTextChanged { _, _, _, _ ->
            if (isTypedByUser) validateColorInput()
        }

    private fun validateColorInput() {
        val color = collectColorInput()
        colorInputVM.validateColor(color)
    }

    private fun collectColorInput(): ColorRgbPresentation {
        val r = binding.inputRgbR.getTextString()?.toIntOrNull()
        val g = binding.inputRgbG.getTextString()?.toIntOrNull()
        val b = binding.inputRgbB.getTextString()?.toIntOrNull()
        return ColorRgbPresentation(
            r = r,
            g = g,
            b = b
        )
    }

    private fun collectColorRgb() =
        colorInputVM.colorRgb.collectOnLifecycle { resource ->
            resource.fold(
                onEmpty = ::onColorRgbEmpty,
                onSuccess = ::onColorRgbSuccess
            )
        }

    private fun onColorRgbEmpty() {
        isTypedByUser = false
        binding.inputRgbR.editText?.text?.clear()
        binding.inputRgbG.editText?.text?.clear()
        binding.inputRgbB.editText?.text?.clear()
        isTypedByUser = true
    }

    private fun onColorRgbSuccess(color: ColorRgbPresentation) {
        isTypedByUser = false
        binding.inputRgbR.editText?.setText(color.r.toString())
        binding.inputRgbG.editText?.setText(color.g.toString())
        binding.inputRgbB.editText?.setText(color.b.toString())
        isTypedByUser = true
    }

    companion object {

        fun newInstance() = ColorInputRgbFragment()
    }
}