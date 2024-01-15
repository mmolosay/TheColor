package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.input.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import io.github.mmolosay.thecolor.presentation.color.ColorPrototype
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputHex
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputViewModel
import io.github.mmolosay.thecolor.utils.Resource
import kotlinx.coroutines.flow.Flow

class ColorInputHexFragment :
    BaseColorInputFragment<ColorPrototype.Hex>() {

//    private val binding by viewBinding(ColorInputHexFragmentBinding::bind)

    private val vm: ColorInputViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                ColorInputHex(vm = vm)
            }
        }


    // region Set views

    override fun setViews() {
//        setInputTextWatcher()
    }

//    private fun setInputTextWatcher() =
//        binding.inputHex.editText?.doOnTextChanged { _, _, _, _ ->
//            outputOnInputChanges()
//        }

    // endregion

    // region BaseColorInputFragment

    override fun assemblePrototype(): ColorPrototype.Hex {
        val input = vm.uiDataFlow.value.input
        return ColorPrototype.Hex(value = input)
    }

    override fun populateViews(color: ColorPrototype.Hex): Unit =
        Unit
//        binding.run {
//            inputHex.editText?.setTextPreservingSelection(color.value) // TODO: resolve
//        }

    override fun clearViews(): Unit =
        Unit
//        binding.run {
//            inputHex.getText()?.clear() TODO: resolve
//        }

    override fun getColorInputFlow(): Flow<Resource<ColorPrototype.Hex>> =
        colorInputVM.inputHex

    // endregion

    companion object {

        fun newInstance() =
            ColorInputHexFragment()
    }
}