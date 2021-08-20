package com.ordolabs.thecolor.ui.fragment.colorinput

import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.michaelbull.result.Result
import com.ordolabs.thecolor.R
import com.ordolabs.thecolor.databinding.FragmentColorInputHexBinding
import com.ordolabs.thecolor.model.ColorHexPresentation
import com.ordolabs.thecolor.ui.fragment.BaseFragment
import com.ordolabs.thecolor.util.ext.getText
import com.ordolabs.thecolor.viewmodel.ColorInputViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ColorInputHexFragment :
    BaseFragment(R.layout.fragment_color_input_hex),
    ColorInputModelFragment {

    private val binding: FragmentColorInputHexBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by viewModel()

    override fun setUp() {
        // nothing is here
    }

    override fun setViews() {
        // nothing is here
    }

    override fun validateColorInput() {
        val color = collectColorInput()
        colorInputVM.validateColor(color)
    }

    override fun processColorInput(): Result<Unit, Boolean> {
        TODO("processColorInput is not implemented")
    }

    private fun collectColorInput(): ColorHexPresentation? {
        val value = binding.inputHex.getText()?.toString() ?: return null
        return ColorHexPresentation(value)
    }

    companion object {
        fun newInstance() = ColorInputHexFragment()
    }
}