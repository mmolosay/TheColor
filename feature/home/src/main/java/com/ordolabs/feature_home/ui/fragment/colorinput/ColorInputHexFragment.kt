package com.ordolabs.feature_home.ui.fragment.colorinput

import androidx.core.widget.doOnTextChanged
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentColorInputHexBinding
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.colorinput.ColorInputHexViewModel
import com.ordolabs.feature_home.viewmodel.colorinput.ColorInputViewModel
import com.ordolabs.thecolor.model.color.ColorHex
import com.ordolabs.thecolor.model.color.empty
import com.ordolabs.thecolor.util.ext.getText
import com.ordolabs.thecolor.util.ext.getTextString
import com.ordolabs.thecolor.util.ext.setTextPreservingSelection
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

// TODO: Extract BaseColorInputFragment?
class ColorInputHexFragment : BaseFragment(R.layout.fragment_color_input_hex) {

    private val binding: FragmentColorInputHexBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()
    private val colorInputHexVM: ColorInputHexViewModel by sharedViewModel()

    private var latestColor = ColorHex.empty()
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
        val color = assembleColor()
        colorInputVM.validateColor(color)
    }

    private fun assembleColor(): ColorHex {
        val input = binding.inputHex.getTextString()
        return ColorHex(value = input).also {
            this.latestColor = it
        }
    }

    private fun collectColorInput() =
        colorInputHexVM.colorHex.collectOnLifecycle { resource ->
            resource.fold(
                onEmpty = ::onColorInputEmpty,
                onSuccess = ::onColorInputSuccess
            )
        }

    @Suppress("UNUSED_PARAMETER")
    private fun onColorInputEmpty(previous: ColorHex?) {
        if (isResumed) return // prevent user interrupting
        this.isTypedByUser = false
        binding.inputHex.getText()?.clear()
        this.isTypedByUser = true
    }

    private fun onColorInputSuccess(color: ColorHex) {
        if (isResumed && color == latestColor) return // prevent user interrupting
        this.isTypedByUser = false
        binding.inputHex.editText?.setTextPreservingSelection(color.value)
        this.isTypedByUser = true
    }

    companion object {

        fun newInstance() =
            ColorInputHexFragment()
    }
}