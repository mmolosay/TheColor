package io.github.mmolosay.thecolor.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnLifecycleDestroyed
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.github.mmolosay.thecolor.presentation.center.ColorCenter
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
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
        ColorCenter(
            vm = colorCenterViewModel,
            details = { ColorDetails(vm = colorDetailsViewModel) },
            scheme = { ColorScheme(vm = colorSchemeViewModel) },
            modifier = Modifier.padding(top = 24.dp),
        )
    }
}