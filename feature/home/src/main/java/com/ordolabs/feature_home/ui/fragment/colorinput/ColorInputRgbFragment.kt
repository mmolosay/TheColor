package com.ordolabs.feature_home.ui.fragment.colorinput

import androidx.core.widget.doOnTextChanged
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentColorInputRgbBinding
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.ColorInputViewModel
import com.ordolabs.thecolor.model.InputRgbPresentation
import com.ordolabs.thecolor.ui.util.inputfilter.PreventingInputFilter
import com.ordolabs.thecolor.ui.util.inputfilter.RangeInputFilter
import com.ordolabs.thecolor.util.ColorUtil
import com.ordolabs.thecolor.util.ColorUtil.toColorRgb
import com.ordolabs.thecolor.util.ext.addFilters
import com.ordolabs.thecolor.util.ext.getText
import com.ordolabs.thecolor.util.ext.getTextString
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ColorInputRgbFragment : BaseFragment(R.layout.fragment_color_input_rgb) {

    private val binding: FragmentColorInputRgbBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()

    private var isTypedByUser = true

    override fun collectViewModelsData() {
        collectColorPreview()
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

    private fun collectColorInput(): InputRgbPresentation {
        val r = binding.inputRgbR.getTextString()?.toIntOrNull()
        val g = binding.inputRgbG.getTextString()?.toIntOrNull()
        val b = binding.inputRgbB.getTextString()?.toIntOrNull()
        return InputRgbPresentation(r, g, b)
    }

    private fun collectColorPreview() =
        colorInputVM.colorPreview.collectOnLifecycle { resource ->
            if (isResumed) return@collectOnLifecycle // prevent user interrupting
            resource.fold(
                onEmpty = ::onColorPreviewEmpty,
                onSuccess = ::onColorPreviewSuccess
            )
        }

    @Suppress("UNUSED_PARAMETER")
    private fun onColorPreviewEmpty(previous: ColorUtil.Color?) {
        isTypedByUser = false
        binding.inputRgbR.getText()?.clear()
        binding.inputRgbG.getText()?.clear()
        binding.inputRgbB.getText()?.clear()
        isTypedByUser = true
    }

    private fun onColorPreviewSuccess(color: ColorUtil.Color) {
        val rgb = color.toColorRgb()
        isTypedByUser = false
        binding.inputRgbR.editText?.setText(rgb.r.toString())
        binding.inputRgbG.editText?.setText(rgb.g.toString())
        binding.inputRgbB.editText?.setText(rgb.b.toString())
        isTypedByUser = true
    }

    companion object {

        fun newInstance() = ColorInputRgbFragment()
    }
}