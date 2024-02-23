package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.scheme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnLifecycleDestroyed
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.presentation.color.data.ColorScheme
import io.github.mmolosay.thecolor.presentation.color.data.ColorSchemeRequest
import io.github.mmolosay.thecolor.presentation.design.ColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.ProvideColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.ColorDataObtainFragment
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.base.BaseColorDataFragment
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.scheme.editor.ColorSchemeEditorFragment
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.scheme.editor.ColorSchemeEditorParent
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.scheme.editor.ColorSchemeEditorView
import io.github.mmolosay.thecolor.presentation.home.viewmodel.color.data.scheme.ColorSchemeObtainViewModel
import io.github.mmolosay.thecolor.presentation.scheme.ColorScheme
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel
import io.github.mmolosay.thecolor.utils.Resource
import kotlinx.coroutines.flow.Flow
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

@AndroidEntryPoint
class ColorSchemeObtainFragment :
    ColorDataObtainFragment<ColorScheme>(),
    ColorSchemeEditorParent {

    private val schemeObtainVM: ColorSchemeObtainViewModel by viewModels()

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
                    // TODO: move ProvideColorsOnTintedSurface() to outside
                    val colors = rememberContentColors(isSurfaceDark = true)
                    ProvideColorsOnTintedSurface(colors) {
                        ColorScheme(vm = colorSchemeViewModel)
                    }
                }
            }
        }

    @Composable
    private fun rememberContentColors(isSurfaceDark: Boolean): ColorsOnTintedSurface =
        remember(isSurfaceDark) { if (isSurfaceDark) colorsOnDarkSurface() else colorsOnLightSurface() }

    //    override fun collectViewModelsData() {
//        // collects from Compose
//    }

    override fun setFragments() {
        // uses Compose, don't need to set Fragments
    }

    // region Set views

    override fun setViews() {
        super.setViews()
        view?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = resources.getDimensionPixelSize(DesignR.dimen.offset_16)
        }
    }

    // endregion

    // region ColorDataObtainFragment

    override fun getColorDataFlow(): Flow<Resource<ColorScheme>> =
        schemeObtainVM.scheme

    override fun obtainColorData() {
        // old
//        val request = assembleSchemeRequest() ?: return
//        schemeObtainVM.getColorScheme(request)

        val color = color ?: return
        val domainColor = Color.Hex(color.hexSignless.toInt(radix = 16))
//        colorSchemeViewModel.getColorScheme(domainColor) // new
    }

    override fun makeColorDataFragment(): BaseColorDataFragment<ColorScheme> =
        ColorSchemeEditorFragment.newInstance()

    override fun makeContentShimmerFragment(): Fragment =
        ColorSchemeShimmerFragment.newInstance()

    private fun assembleSchemeRequest(): ColorSchemeRequest? {
        val config = getColorSchemeEditorView()?.appliedConfig ?: return null
        return assebleSchemeRequest(config)
    }

    // endregion

    // region ColorSchemeEditorParent

    override fun dispatchColorSchemeConfig(config: ColorSchemeRequest.Config) {
        obtainColorScheme(config)
    }

    // endregion

    private fun getColorSchemeEditorView(): ColorSchemeEditorView? =
        dataView as? ColorSchemeEditorView

    private fun obtainColorScheme(config: ColorSchemeRequest.Config) {
        val request = assebleSchemeRequest(config) ?: return
        schemeObtainVM.getColorScheme(request)
    }

    private fun assebleSchemeRequest(config: ColorSchemeRequest.Config): ColorSchemeRequest? {
        val seed = color ?: return null
        return ColorSchemeRequest(seed, config)
    }

    companion object {
        fun newInstance() =
            ColorSchemeObtainFragment()
    }
}