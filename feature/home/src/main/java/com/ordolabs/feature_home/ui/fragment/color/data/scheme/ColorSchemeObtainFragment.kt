package com.ordolabs.feature_home.ui.fragment.color.data.scheme

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.ordolabs.feature_home.ui.fragment.color.data.ColorDataObtainFragment
import com.ordolabs.feature_home.ui.fragment.color.data.base.BaseColorDataFragment
import com.ordolabs.feature_home.ui.fragment.color.data.scheme.editor.ColorSchemeEditorFragment
import com.ordolabs.feature_home.util.FeatureHomeUtil.featureHomeComponent
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeEditorViewModel
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeObtainViewModel
import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.model.color.data.ColorSchemeRequest
import com.ordolabs.thecolor.util.ext.ownViewModels
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.getOrNull
import kotlinx.coroutines.flow.Flow
import com.ordolabs.thecolor.R as RApp

class ColorSchemeObtainFragment :
    ColorDataObtainFragment<ColorScheme>() {

    private val schemeObtainVM: ColorSchemeObtainViewModel by ownViewModels {
        featureHomeComponent.viewModelFactory
    }
    private val schemeEditorVM: ColorSchemeEditorViewModel by viewModels {
        featureHomeComponent.viewModelFactory
    }

    // region Collect ViewModels data

    override fun collectViewModelsData() {
        super.collectViewModelsData()
        collectDispatchConfigCommand()
    }

    private fun collectDispatchConfigCommand() =
        schemeEditorVM.dispatchConfigCommand.collectOnLifecycle(Lifecycle.State.RESUMED) { resource ->
            resource.ifSuccess { obtainColorData() }
        }

    // endregion

    // region Set views

    override fun setViews() {
        super.setViews()
        view?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = resources.getDimensionPixelSize(RApp.dimen.offset_16)
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
        val seed = color ?: return null
        val config = schemeEditorVM.config.value.getOrNull() ?: return null
        return ColorSchemeRequest(seed, config)
    }

    // endregion

    companion object {
        fun newInstance() =
            ColorSchemeObtainFragment()
    }
}