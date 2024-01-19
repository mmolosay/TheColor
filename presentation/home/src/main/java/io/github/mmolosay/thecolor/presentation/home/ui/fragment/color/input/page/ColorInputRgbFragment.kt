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
import io.github.mmolosay.thecolor.input.rgb.ColorInputRgb
import io.github.mmolosay.thecolor.input.rgb.ColorInputRgbViewData
import io.github.mmolosay.thecolor.input.rgb.ColorInputRgbViewModel
import io.github.mmolosay.thecolor.utils.Resource
import kotlinx.coroutines.flow.Flow

@AndroidEntryPoint
class ColorInputRgbFragment :
    BaseColorInputFragment<ColorPrototype.Rgb>() {

//    private val binding by viewBinding(ColorInputRgbFragmentBinding::bind)

    private val vm: ColorInputRgbViewModel by viewModels(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<ColorInputRgbViewModel.Factory> { factory ->
                val viewData = ColorInputRgbViewData(requireContext())
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
                    ColorInputRgb(vm)
                }
            }
        }

    // region Set views

    override fun setViews() {
//        setComponentInputsFilters()
//        setComponentRTextWatcher()
//        setComponentGTextWatcher()
//        setComponentBTextWatcher()
    }

//    private fun setComponentInputsFilters() = binding.run {
//        val range = RangeInputFilter(min = 0, max = 255)
//        val preventing = PreventingInputFilter(preceding = "0", what = "0")
//        inputRgbR.editText?.addFilters(range, preventing)
//        inputRgbG.editText?.addFilters(range, preventing)
//        inputRgbB.editText?.addFilters(range, preventing)
//    }
//
//    private fun setComponentRTextWatcher() =
//        binding.inputRgbR.editText?.doOnTextChanged { _, _, _, _ ->
//            outputOnInputChanges()
//        }
//
//    private fun setComponentGTextWatcher() =
//        binding.inputRgbG.editText?.doOnTextChanged { _, _, _, _ ->
//            outputOnInputChanges()
//        }
//
//    private fun setComponentBTextWatcher() =
//        binding.inputRgbB.editText?.doOnTextChanged { _, _, _, _ ->
//            outputOnInputChanges()
//        }

    // endregion

    // region BaseColorInputFragment

    override fun assemblePrototype(): ColorPrototype.Rgb {
//        val r = binding.inputRgbR.getTextString()?.toIntOrNull()
//        val g = binding.inputRgbG.getTextString()?.toIntOrNull()
//        val b = binding.inputRgbB.getTextString()?.toIntOrNull()
//        return ColorPrototype.Rgb(r, g, b)
        return ColorPrototype.Rgb(null, null, null)
    }

    override fun populateViews(color: ColorPrototype.Rgb): Unit =
        Unit
//        binding.run {
//            inputRgbR.editText?.setTextPreservingSelection(color.r.toString())
//            inputRgbG.editText?.setTextPreservingSelection(color.g.toString())
//            inputRgbB.editText?.setTextPreservingSelection(color.b.toString())
//        }

    override fun clearViews(): Unit =
        Unit
//        binding.run {
//            binding.inputRgbR.getText()?.clear()
//            binding.inputRgbG.getText()?.clear()
//            binding.inputRgbB.getText()?.clear()
//        }

    override fun getColorInputFlow(): Flow<Resource<ColorPrototype.Rgb>> =
        colorInputVM.inputRgb

    // endregion

    companion object {

        fun newInstance() =
            ColorInputRgbFragment()
    }
}