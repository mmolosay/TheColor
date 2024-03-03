package io.github.mmolosay.thecolor.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnLifecycleDestroyed
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.github.mmolosay.thecolor.presentation.center.ColorCenter
import io.github.mmolosay.thecolor.presentation.center.ColorCenterShape
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.design.ColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.ProvideColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.details.ColorDetails
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.input.ColorInput
import io.github.mmolosay.thecolor.presentation.input.ColorInputViewModel
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbViewModel
import io.github.mmolosay.thecolor.presentation.preview.ColorPreview
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewViewModel
import io.github.mmolosay.thecolor.presentation.scheme.ColorScheme
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel
import androidx.compose.ui.graphics.Color as ComposeColor

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()

    private val colorInputViewModel: ColorInputViewModel by viewModels()
    private val colorInputHexViewModel: ColorInputHexViewModel by viewModels()
    private val colorInputRgbViewModel: ColorInputRgbViewModel by viewModels()

    private val colorPreviewViewModel: ColorPreviewViewModel by viewModels()

    private val colorCenterViewModel: ColorCenterViewModel by viewModels()
    private val colorDetailsViewModel: ColorDetailsViewModel by viewModels()
    private val colorSchemeViewModel: ColorSchemeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(inflater.context).apply {
            setViewCompositionStrategy(DisposeOnLifecycleDestroyed(lifecycle))
            setContent {
                TheColorTheme {
                    HomeScreen(
                        vm = homeViewModel,
                        colorInput = { ColorInput() },
                        colorPreview = { ColorPreview() },
                        colorCenter = { ColorCenter() },
                    )
                }
            }
        }

    @Composable
    private fun ColorInput() =
        ColorInput(
            vm = colorInputViewModel,
            hexViewModel = colorInputHexViewModel,
            rgbViewModel = colorInputRgbViewModel,
        )

    @Composable
    private fun ColorPreview() =
        ColorPreview(vm = colorPreviewViewModel)

    @Composable
    private fun ColorCenter() {
        // TODO: move ProvideColorsOnTintedSurface() to outside?
        val colors = rememberContentColors(useLight = true) // TODO: use real value
        ProvideColorsOnTintedSurface(colors) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        clip = true
                        shape = ColorCenterShape
                    }
                    .background(ComposeColor(0xFF_123456)) // TODO: use real color
            ) {
                ColorCenter(
                    vm = colorCenterViewModel,
                    details = { ColorDetails(vm = colorDetailsViewModel) },
                    scheme = { ColorScheme(vm = colorSchemeViewModel) },
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
        }
    }

    @Composable
    private fun rememberContentColors(useLight: Boolean): ColorsOnTintedSurface =
        remember(useLight) {
            if (useLight) colorsOnDarkSurface() else colorsOnLightSurface()
        }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
//        this.state = createStateByType(homeVM.stateType)
//        state.restoreState()
    }

    override fun onStop() {
        super.onStop()
//        hideSoftInputAndClearFocus()
    }

    companion object {

        // being created by NavHostFragment, thus no newInstance() method
    }
}