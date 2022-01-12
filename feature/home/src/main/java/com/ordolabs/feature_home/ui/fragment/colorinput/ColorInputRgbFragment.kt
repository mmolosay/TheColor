package com.ordolabs.feature_home.ui.fragment.colorinput

import androidx.core.widget.doOnTextChanged
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentColorInputRgbBinding
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.colorinput.ColorInputRgbViewModel
import com.ordolabs.feature_home.viewmodel.colorinput.ColorInputViewModel
import com.ordolabs.thecolor.model.color.ColorRgb
import com.ordolabs.thecolor.model.colorinput.ColorInputRgb
import com.ordolabs.thecolor.ui.util.inputfilter.PreventingInputFilter
import com.ordolabs.thecolor.ui.util.inputfilter.RangeInputFilter
import com.ordolabs.thecolor.util.ext.addFilters
import com.ordolabs.thecolor.util.ext.getText
import com.ordolabs.thecolor.util.ext.getTextString
import com.ordolabs.thecolor.util.ext.setTextPreservingSelection
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ColorInputRgbFragment : BaseFragment(R.layout.fragment_color_input_rgb) {

    private val binding: FragmentColorInputRgbBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()
    private val colorInputRgbVM: ColorInputRgbViewModel by sharedViewModel()

    private var isTypedByUser = true

    override fun collectViewModelsData() {
        collectColorInput()
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
        val color = makeColorRgbPresentation()
        colorInputVM.validateColor(color)
    }

    private fun makeColorRgbPresentation(): ColorRgb {
        val r = binding.inputRgbR.getTextString()?.toIntOrNull()
        val g = binding.inputRgbG.getTextString()?.toIntOrNull()
        val b = binding.inputRgbB.getTextString()?.toIntOrNull()
        return ColorRgb(r, g, b)
    }

    private fun collectColorInput() =
        colorInputRgbVM.colorRgb.collectOnLifecycle { resource ->
            resource.fold(
                onEmpty = ::onColorInputEmpty,
                onSuccess = ::onColorInputSuccess
            )
        }

    @Suppress("UNUSED_PARAMETER")
    private fun onColorInputEmpty(previous: ColorInputRgb?) {
        if (isResumed) return // prevent user interrupting
        this.isTypedByUser = false
        binding.inputRgbR.getText()?.clear()
        binding.inputRgbG.getText()?.clear()
        binding.inputRgbB.getText()?.clear()
        this.isTypedByUser = true
    }

    private fun onColorInputSuccess(input: ColorInputRgb) {
        if (isResumed && input.isUserInput) return // prevent user interrupting
        val color = input.color
        this.isTypedByUser = false
        binding.inputRgbR.editText?.setTextPreservingSelection(color.r.toString())
        binding.inputRgbG.editText?.setTextPreservingSelection(color.g.toString())
        binding.inputRgbB.editText?.setTextPreservingSelection(color.b.toString())
        this.isTypedByUser = true
    }

    companion object {

        fun newInstance() = ColorInputRgbFragment()
    }
}