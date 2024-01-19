package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.input.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnLifecycleDestroyed
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import io.github.mmolosay.thecolor.presentation.color.ColorPrototype
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.input.hex.ColorInputHex
import io.github.mmolosay.thecolor.input.hex.ColorInputHexViewData
import io.github.mmolosay.thecolor.input.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.utils.Resource
import kotlinx.coroutines.flow.Flow

@AndroidEntryPoint
class ColorInputHexFragment :
    BaseColorInputFragment<ColorPrototype.Hex>() {

//    private val binding by viewBinding(ColorInputHexFragmentBinding::bind)

    private val vm: ColorInputHexViewModel by viewModels(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<ColorInputHexViewModel.Factory> { factory ->
                val viewData = ColorInputHexViewData(requireContext())
                factory.create(viewData)
            }
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(inflater.context).apply {
            setViewCompositionStrategy(DisposeOnLifecycleDestroyed(lifecycle))
            setContent {
                TheColorTheme {
                    ColorInputHex(vm)
                }
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
//        val input = vm.uiDataFlow.value?.inputField?.text
//        return ColorPrototype.Hex(value = input)
        return ColorPrototype.Hex(value = null)
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