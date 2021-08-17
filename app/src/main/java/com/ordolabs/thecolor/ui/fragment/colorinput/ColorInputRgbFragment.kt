package com.ordolabs.thecolor.ui.fragment.colorinput

import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.michaelbull.result.Result
import com.ordolabs.thecolor.R
import com.ordolabs.thecolor.databinding.FragmentColorInputRgbBinding
import com.ordolabs.thecolor.ui.fragment.BaseFragment
import com.ordolabs.thecolor.viewmodel.ColorInputViewModel

class ColorInputRgbFragment :
    BaseFragment(R.layout.fragment_color_input_rgb),
    ColorInputModelFragment {

    private val binding: FragmentColorInputRgbBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by viewModels()

    override fun setUp() {
//        TODO("setUp is not implemented")
    }

    override fun setViews() {
//        TODO("setViews is not implemented")
    }

    override fun isColorInputValid(): Boolean {
        TODO("isColorInputValid is not implemented")
    }

    override fun processColorInput(): Result<Unit, Boolean> {
        TODO("processColorInput is not implemented")
    }

    companion object {
        fun newInstance() = ColorInputRgbFragment()
    }
}