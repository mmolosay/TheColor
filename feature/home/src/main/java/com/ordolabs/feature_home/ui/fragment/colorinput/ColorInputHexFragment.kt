package com.ordolabs.feature_home.ui.fragment.colorinput

import androidx.core.widget.doOnTextChanged
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentColorInputHexBinding
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.colorinput.ColorInputHexViewModel
import com.ordolabs.feature_home.viewmodel.colorinput.ColorInputViewModel
import com.ordolabs.thecolor.model.color.ColorHexPresentation
import com.ordolabs.thecolor.model.colorinput.ColorInputHexPresentation
import com.ordolabs.thecolor.util.ext.getText
import com.ordolabs.thecolor.util.ext.getTextString
import com.ordolabs.thecolor.util.ext.setTextPreservingSelection
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ColorInputHexFragment : BaseFragment(R.layout.fragment_color_input_hex) {

    private val binding: FragmentColorInputHexBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()
    private val colorInputHexVM: ColorInputHexViewModel by sharedViewModel()

    private var isTypedByUser = true

    override fun collectViewModelsData() {
        collectColorInput()
    }

    override fun setViews() {
        setInputTextWatcher()
    }

    private fun setInputTextWatcher() =
        binding.inputHex.editText?.doOnTextChanged { _, _, _, _ ->
            if (isTypedByUser) validateColorInput()
        }

    private fun validateColorInput() {
        val color = makeColorHexPresentation()
        colorInputVM.validateColor(color)
    }

    private fun makeColorHexPresentation(): ColorHexPresentation {
        val input = binding.inputHex.getTextString()
        return ColorHexPresentation(value = input)
    }

    private fun collectColorInput() =
        colorInputHexVM.colorHex.collectOnLifecycle { resource ->
            resource.fold(
                onEmpty = ::onColorInputEmpty,
                onSuccess = ::onColorInputSuccess
            )
        }

    @Suppress("UNUSED_PARAMETER")
    private fun onColorInputEmpty(previous: ColorInputHexPresentation?) {
        if (isResumed) return // prevent user interrupting
        this.isTypedByUser = false
        binding.inputHex.getText()?.clear()
        this.isTypedByUser = true
    }

    private fun onColorInputSuccess(input: ColorInputHexPresentation) {
        if (isResumed && input.isUserInput) return // prevent user interrupting
        val color = input.color
        this.isTypedByUser = false
        binding.inputHex.editText?.setTextPreservingSelection(color.value)
        this.isTypedByUser = true
    }

    companion object {

        fun newInstance() =
            ColorInputHexFragment()
    }
}