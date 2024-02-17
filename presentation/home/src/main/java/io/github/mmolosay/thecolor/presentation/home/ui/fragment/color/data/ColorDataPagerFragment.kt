package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data

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
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.github.mmolosay.thecolor.domain.model.Color
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
import io.github.mmolosay.thecolor.presentation.fragment.BaseFragment
import io.github.mmolosay.thecolor.presentation.home.viewmodel.color.data.ColorDataViewModel
import io.github.mmolosay.thecolor.presentation.scheme.ColorScheme
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel
import androidx.compose.ui.graphics.Color as ComposeColor
import io.github.mmolosay.thecolor.presentation.color.Color as OldColor

@AndroidEntryPoint
class ColorDataPagerFragment :
    BaseFragment(),
    ColorThemedView {

    //    private val binding by viewBinding(ColorDataPagerFragmentBinding::bind)
    private val colorDataVM: ColorDataViewModel by viewModels()

    private val colorDetailsViewModel: ColorDetailsViewModel by viewModels()
    private val colorSchemeViewModel: ColorSchemeViewModel by viewModels()
    private val colorCenterViewModel: ColorCenterViewModel by viewModels()

    override var color: OldColor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(inflater.context).apply {
            setViewCompositionStrategy(DisposeOnLifecycleDestroyed(lifecycle))
            setContent {
                TheColorTheme {
                    // TODO: move ProvideColorsOnTintedSurface() to outside?
                    val colors = rememberContentColors(isSurfaceDark = true) // TODO: use real value
                    ProvideColorsOnTintedSurface(colors) {
                        ColorCenter()
                    }
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val oldColor = color ?: return
        val color = Color.Hex(oldColor.hexSignless.toInt(radix = 16))
        colorDetailsViewModel.getColorDetails(color)
        colorSchemeViewModel.getColorScheme(color)
    }

    @Composable
    private fun ColorCenter() {
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

    @Composable
    private fun rememberContentColors(isSurfaceDark: Boolean): ColorsOnTintedSurface =
        remember(isSurfaceDark) {
            if (isSurfaceDark) colorsOnDarkSurface() else colorsOnLightSurface()
        }

    // region Set up

    override fun setUp() {
        parseArguments()
    }

    private fun parseArguments() {
        val args = arguments ?: return
        parseColor(args)
    }

    private fun parseColor(args: Bundle) {
        val key = ARGUMENTS_KEY_COLOR
        if (!args.containsKey(key)) return
        this.color = args.getParcelable(key)
    }

    // endregion

    // region Collect ViewModels data

    override fun collectViewModelsData() {
        collectChangePageCommand()
    }

    private fun collectChangePageCommand() =
        colorDataVM.changePageCommand.collectOnLifecycle { resource ->
            resource.ifSuccess { page ->
//                binding.pager.setCurrentItem(page.ordinal, /*smoothScroll*/ true)
            }
        }

    // endregion

    // region Set views

    override fun setViews() {
        setViewPager()
    }

    private fun setViewPager() = Unit
//        binding.pager.let { pager ->
//            val adapter = ColorDataPagerAdapter(this)
//            pager.adapter = adapter
//            pager.offscreenPageLimit = adapter.itemCount
//        }

    // endregion

    companion object {

        private const val ARGUMENTS_KEY_COLOR = "ARGUMENTS_KEY_COLOR"

        fun newInstance(color: OldColor?) =
            ColorDataPagerFragment().apply {
                arguments = bundleOf(
                    ARGUMENTS_KEY_COLOR to color
                )
            }
    }
}