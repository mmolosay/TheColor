package com.ordolabs.feature_home.ui.fragment.colorinput

import androidx.core.widget.doOnTextChanged
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentColorInputRgbBinding
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.ColorInputViewModel
import com.ordolabs.feature_home.viewmodel.ColorInputViewModel.ColorPreview
import com.ordolabs.thecolor.model.InputRgbPresentation
import com.ordolabs.thecolor.ui.util.inputfilter.PreventingInputFilter
import com.ordolabs.thecolor.ui.util.inputfilter.RangeInputFilter
import com.ordolabs.thecolor.util.ext.addFilters
import com.ordolabs.thecolor.util.ext.getText
import com.ordolabs.thecolor.util.ext.getTextString
import com.ordolabs.thecolor.util.ext.setTextPreservingSelection
import com.ordolabs.thecolor.util.struct.toColorRgb
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
            resource.fold(
                onEmpty = ::onColorPreviewEmpty,
                onSuccess = ::onColorPreviewSuccess
            )
        }

    @Suppress("UNUSED_PARAMETER")
    private fun onColorPreviewEmpty(previous: ColorPreview?) {
        if (isResumed) return // prevent user interrupting
        isTypedByUser = false
        binding.inputRgbR.getText()?.clear()
        binding.inputRgbG.getText()?.clear()
        binding.inputRgbB.getText()?.clear()
        isTypedByUser = true
    }

    private fun onColorPreviewSuccess(colorPreview: ColorPreview) {
        if (isResumed && colorPreview.isUserInput) return // prevent user interrupting
        val rgb = colorPreview.color.toColorRgb()
        isTypedByUser = false
        binding.inputRgbR.editText?.setTextPreservingSelection(rgb.r.toString())
        binding.inputRgbG.editText?.setTextPreservingSelection(rgb.g.toString())
        binding.inputRgbB.editText?.setTextPreservingSelection(rgb.b.toString())
        isTypedByUser = true
    }

    companion object {

        fun newInstance() = ColorInputRgbFragment()
    }
}