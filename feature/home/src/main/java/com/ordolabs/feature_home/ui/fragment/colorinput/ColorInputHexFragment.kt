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

class ColorInputHexFragment : BaseFragment(R.layout.fragment_color_input_hex) {

    private val binding: FragmentColorInputHexBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()

    private var isTypedByUser = true

    override fun collectViewModelsData() {
        collectColorHex()
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

    private fun collectColorInput(): ColorHexPresentation? {
        val input = binding.inputHex.getTextString()
        val value = input?.takeUnless { it.isEmpty() } ?: return null
        return ColorHexPresentation(value)
    }

    private fun collectColorHex() =
        colorInputVM.colorHex.collectOnLifecycle { resource ->
            resource.fold(
                onEmpty = ::onColorHexEmpty,
                onSuccess = ::onColorHexSuccess
            )
        }

    private fun onColorHexEmpty() {
        isTypedByUser = false
        binding.inputHex.editText?.text?.clear()
        isTypedByUser = true
    }

    private fun onColorHexSuccess(color: ColorHexPresentation) {
        isTypedByUser = false
        binding.inputHex.editText?.setText(color.value)
        isTypedByUser = true
    }

    companion object {

        fun newInstance() = ColorInputHexFragment()
    }
}