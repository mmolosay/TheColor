package com.ordolabs.feature_home.ui.fragment.colordata.scheme

import androidx.fragment.app.Fragment
import com.ordolabs.feature_home.ui.fragment.colordata.ColorDataObtainFragment
import com.ordolabs.feature_home.ui.fragment.colordata.base.BaseColorDataFragment
import com.ordolabs.feature_home.ui.fragment.colordata.scheme.editor.ColorSchemeEditorFragment
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeEditorViewModel
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeObtainViewModel
import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.model.color.data.ColorSchemeRequest
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.getOrNull
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ColorSchemeObtainFragment :
    ColorDataObtainFragment<ColorScheme>() {

    private val schemeObtainVM: ColorSchemeObtainViewModel by sharedViewModel()
    private val schemeEditorVM: ColorSchemeEditorViewModel by sharedViewModel()

    override fun collectViewModelsData() {
        super.collectViewModelsData()
        collectDispatchConfigCommand()
    }

    private fun collectDispatchConfigCommand() =
        schemeEditorVM.dispatchConfigCommand.collectOnLifecycle { resource ->
            resource.ifSuccess { obtainColorData() }
        }

    private fun assembleSchemeRequest(): ColorSchemeRequest? {
        val seed = color ?: return null
        val config = schemeEditorVM.config.value.getOrNull() ?: return null
        return ColorSchemeRequest(seed, config)
    }

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

    // endregion

    companion object {
        fun newInstance() =
            ColorSchemeObtainFragment()
    }
}