package com.ordolabs.feature_home.ui.fragment.colorinput

import androidx.core.widget.doOnTextChanged
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentColorInputHexBinding
import com.ordolabs.feature_home.viewmodel.colorinput.ColorInputHexViewModel
import com.ordolabs.thecolor.model.color.ColorHex
import com.ordolabs.thecolor.util.ext.getText
import com.ordolabs.thecolor.util.ext.getTextString
import com.ordolabs.thecolor.util.ext.setTextPreservingSelection
import com.ordolabs.thecolor.util.struct.Resource
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ColorInputHexFragment :
    BaseColorInputFragment<ColorHex>(R.layout.fragment_color_input_hex) {

    private val binding: FragmentColorInputHexBinding by viewBinding()
    private val colorInputHexVM: ColorInputHexViewModel by sharedViewModel()

    override fun setViews() {
        setInputTextWatcher()
    }

    private fun setInputTextWatcher() =
        binding.inputHex.editText?.doOnTextChanged { _, _, _, _ ->
            if (isTypedByUser) validateColorInput()
        }

    // region BaseColorInputFragment

    override fun assembleColor(): ColorHex {
        val input = binding.inputHex.getTextString()
        return ColorHex(value = input)
    }

    override fun populateViews(color: ColorHex): Unit =
        binding.run {
            inputHex.editText?.setTextPreservingSelection(color.value)
        }

    override fun clearViews(): Unit =
        binding.run {
            inputHex.getText()?.clear()
        }

    override fun getColorInputFlow(): Flow<Resource<ColorHex>> =
        colorInputHexVM.colorHex

    // endregion

    companion object {

        fun newInstance() =
            ColorInputHexFragment()
    }
}