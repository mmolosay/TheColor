package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.input.pager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.ViewCompositionStrategy.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.presentation.input.ColorInput
import io.github.mmolosay.thecolor.presentation.input.ColorInputMapper
import io.github.mmolosay.thecolor.presentation.input.ColorInputViewModel
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbViewModel
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.fragment.BaseFragment
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.input.page.ColorInputParent
import io.github.mmolosay.thecolor.presentation.util.ext.requireParentFragmentOfType
import kotlinx.coroutines.launch
import javax.inject.Inject
import io.github.mmolosay.thecolor.presentation.color.Color as OldColor
import io.github.mmolosay.thecolor.presentation.color.ColorPrototype as OldColorPrototype

/**
 * `Fragment` with `ViewPager` which contains `Fragment`s of specific color scheme inputs.
 * Requires parent `Fragment` to be an instance of [ColorInputParent].
 */
@AndroidEntryPoint
class ColorInputPagerFragment :
    BaseFragment(),
    ColorInputPagerView,
    ColorInputParent {

    @Inject
    lateinit var colorInputMapper: ColorInputMapper

    @Inject
    lateinit var colorConverter: ColorConverter

    private val parent: ColorInputParent? by lazy { requireParentFragmentOfType() }

    private val colorInputViewModel: ColorInputViewModel by viewModels()
    private val hexViewModel: ColorInputHexViewModel by viewModels()
    private val rgbViewModel: ColorInputRgbViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(inflater.context).apply {
            setViewCompositionStrategy(DisposeOnLifecycleDestroyed(lifecycle))
            setContent {
                TheColorTheme {
                    ColorInput(
                        vm = colorInputViewModel,
                        hexViewModel = hexViewModel,
                        rgbViewModel = rgbViewModel,
                    )
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectCurrentColorFlow()
    }

    private fun collectCurrentColorFlow() =
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                colorInputViewModel.currentColorFlow.collect(::currentColorFlowCollector)
            }
        }

    private fun currentColorFlowCollector(color: Color.Abstract?) {
        if (color != null) {
            val oldPrototype = color.toOldColorPrototype()
            onInputChanged(oldPrototype)
        } else {
            onInputChanged(OldColorPrototype.Hex(value = null))
        }
    }

    private fun Color.Abstract.toOldColorPrototype(): OldColorPrototype {
        val colorHex = with(colorConverter) { toHex() }
        val colorInputHex = with(colorInputMapper) { colorHex.toColorInput() }
        return OldColorPrototype.Hex(value = colorInputHex.string)
    }

    // region ColorInputPagerView

    override fun updateCurrentColor(color: OldColor) {
        // TODO: this feature doesn't work temporarily. Update after View UI is gone.
    }

    override fun clearCurrentColor() {
        // TODO: this feature doesn't work temporarily. Update after View UI is gone.
    }

    // endregion

    // region ColorInputParent

    override fun onInputChanged(input: OldColorPrototype) {
        parent?.onInputChanged(input)
    }

    // endregion

    companion object {
        fun newInstance() = ColorInputPagerFragment()
    }
}