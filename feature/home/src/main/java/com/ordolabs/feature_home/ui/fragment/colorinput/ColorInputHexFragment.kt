package com.ordolabs.feature_home.ui.fragment.colorinput

import androidx.core.widget.doOnTextChanged
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentColorInputHexBinding
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.viewmodel.ColorInputViewModel
import com.ordolabs.thecolor.model.ColorHexPresentation
import com.ordolabs.thecolor.util.ext.getTextString
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ColorInputHexFragment :
    BaseFragment(R.layout.fragment_color_input_hex),
    ColorInputModelFragment {

    private val binding: FragmentColorInputHexBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()

    private var isTypedByUser = true

    override fun setUp() {
        observeColorHex()
    }

    override fun setViews() {
        setInputTextWatcher()
    }

    private fun setInputTextWatcher() =
        binding.inputHex.editText?.doOnTextChanged { _, _, _, _ ->
            if (isTypedByUser) validateColorInput()
        }

    override fun validateColorInput() {
        val color = collectColorInput()
        colorInputVM.validateColor(color)
    }

    private fun collectColorInput(): ColorHexPresentation {
        val input = binding.inputHex.getTextString()
        val value = input?.takeUnless { it.isEmpty() } ?: DEFAULT_INPUT_VALUE_HEX
        return ColorHexPresentation(value)
    }

    private fun observeColorHex() = colorInputVM.getColorHex().observe(this) { result ->
        result.ifSuccess { color ->
            isTypedByUser = false
            binding.inputHex.editText?.setText(color.value)
            isTypedByUser = true
        }
    }

    companion object {

        private const val DEFAULT_INPUT_VALUE_HEX = "000000"

        fun newInstance() = ColorInputHexFragment()
    }
}