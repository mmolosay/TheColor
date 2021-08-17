package com.ordolabs.thecolor.ui.fragment.colorinput

import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.michaelbull.result.Result
import com.ordolabs.thecolor.R
import com.ordolabs.thecolor.databinding.FragmentColorInputHexBinding
import com.ordolabs.thecolor.model.ColorHexPresentation
import com.ordolabs.thecolor.ui.fragment.BaseFragment
import com.ordolabs.thecolor.util.ext.getText
import com.ordolabs.thecolor.viewmodel.ColorInputViewModel

class ColorInputHexFragment :
    BaseFragment(R.layout.fragment_color_input_hex),
    ColorInputModelFragment {

    private val binding: FragmentColorInputHexBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by viewModels()

    override fun setUp() {
//        TODO("setUp is not implemented")
    }

    override fun setViews() {
//        TODO("setViews is not implemented")
    }

    override fun isColorInputValid(): Boolean {
        val color = collectColorInput() ?: return false
        return colorInputVM.isColorValid(color)
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