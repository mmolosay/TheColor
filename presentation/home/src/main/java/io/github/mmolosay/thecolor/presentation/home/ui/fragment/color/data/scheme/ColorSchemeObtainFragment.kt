package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.scheme

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.github.mmolosay.thecolor.presentation.home.R
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.ColorDataObtainFragment
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.base.BaseColorDataFragment
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.scheme.editor.ColorSchemeEditorFragment
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.scheme.editor.ColorSchemeEditorParent
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.scheme.editor.ColorSchemeEditorView
import io.github.mmolosay.thecolor.presentation.home.viewmodel.color.data.scheme.ColorSchemeObtainViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.github.mmolosay.thecolor.presentation.color.data.ColorScheme
import io.github.mmolosay.thecolor.presentation.color.data.ColorSchemeRequest
import io.github.mmolosay.thecolor.utils.Resource
import kotlinx.coroutines.flow.Flow

@AndroidEntryPoint
class ColorSchemeObtainFragment :
    ColorDataObtainFragment<ColorScheme>(),
    ColorSchemeEditorParent {

    private val schemeObtainVM: ColorSchemeObtainViewModel by viewModels()

    // region Set views

    override fun setViews() {
        super.setViews()
        view?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = resources.getDimensionPixelSize(R.dimen.offset_16)
        }
    }

    // endregion

    // region ColorDataObtainFragment

    override fun getColorDataFlow(): Flow<Resource<ColorScheme>> =
        schemeObtainVM.scheme

    override fun obtainColorData() {
        val request = assembleSchemeRequest() ?: return
        schemeObtainVM.getColorScheme(request)
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