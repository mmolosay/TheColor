package com.ordolabs.feature_home.ui.fragment.color.input.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorInputHexFragmentBinding
import io.github.mmolosay.presentation.model.color.ColorPrototype
import io.github.mmolosay.presentation.util.ext.getText
import io.github.mmolosay.presentation.util.ext.getTextString
import io.github.mmolosay.presentation.util.ext.setTextPreservingSelection
import com.ordolabs.util.Resource
import kotlinx.coroutines.flow.Flow

class ColorInputHexFragment :
    BaseColorInputFragment<ColorPrototype.Hex>() {

    private val binding by viewBinding(ColorInputHexFragmentBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.color_input_hex_fragment, container, false)
    }

    // region Set views

    override fun setViews() {
        setInputTextWatcher()
    }

    private fun setInputTextWatcher() =
        binding.inputHex.editText?.doOnTextChanged { _, _, _, _ ->
            outputOnInputChanges()
        }

    // endregion

    // region BaseColorInputFragment

    override fun assemblePrototype(): ColorPrototype.Hex {
        val input = binding.inputHex.getTextString()
        return ColorPrototype.Hex(value = input)
    }

    override fun populateViews(color: ColorPrototype.Hex): Unit =
        binding.run {
            inputHex.editText?.setTextPreservingSelection(color.value)
        }

    override fun clearViews(): Unit =
        binding.run {
            inputHex.getText()?.clear()
        }

    override fun getColorInputFlow(): Flow<Resource<ColorPrototype.Hex>> =
        colorInputVM.inputHex

    // endregion

    companion object {

        fun newInstance() =
            ColorInputHexFragment()
    }
}