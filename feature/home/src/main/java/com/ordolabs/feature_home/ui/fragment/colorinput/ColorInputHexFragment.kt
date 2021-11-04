package com.ordolabs.feature_home.ui.fragment.colorinput

import androidx.core.widget.doOnTextChanged
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentColorInputHexBinding
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.ColorInputViewModel
import com.ordolabs.feature_home.viewmodel.ColorInputViewModel.ColorPreview
import com.ordolabs.thecolor.model.InputHexPresentation
import com.ordolabs.thecolor.util.ColorUtil.toColorHex
import com.ordolabs.thecolor.util.ext.getText
import com.ordolabs.thecolor.util.ext.getTextString
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ColorInputHexFragment : BaseFragment(R.layout.fragment_color_input_hex) {

    private val binding: FragmentColorInputHexBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()

    private var isTypedByUser = true

    override fun collectViewModelsData() {
        collectColorPreview()
    }

    override fun setViews() {
        setInputTextWatcher()
    }

    private fun setInputTextWatcher() =
        binding.inputHex.editText?.doOnTextChanged { _, _, _, _ ->
            if (isTypedByUser) validateColorInput()
        }

    private fun validateColorInput() {
        val color = collectColorInput()
        colorInputVM.validateColor(color)
    }

    private fun collectColorInput(): InputHexPresentation {
        val input = binding.inputHex.getTextString()
        return InputHexPresentation(value = input)
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
        binding.inputHex.getText()?.clear()
        isTypedByUser = true
    }

    private fun onColorPreviewSuccess(colorPreview: ColorPreview) {
        if (isResumed && colorPreview.isUserInput) return // prevent user interrupting
        val hex = colorPreview.color.toColorHex()
        isTypedByUser = false
        binding.inputHex.editText?.setText(hex.value)
        isTypedByUser = true
    }

    companion object {

        fun newInstance() = ColorInputHexFragment()
    }
}