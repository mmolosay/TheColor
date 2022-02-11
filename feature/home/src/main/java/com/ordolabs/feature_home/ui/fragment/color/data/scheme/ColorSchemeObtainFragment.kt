package com.ordolabs.feature_home.ui.fragment.color.data.scheme

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ordolabs.feature_home.ui.fragment.color.data.ColorDataObtainFragment
import com.ordolabs.feature_home.ui.fragment.color.data.base.BaseColorDataFragment
import com.ordolabs.feature_home.ui.fragment.color.data.scheme.editor.ColorSchemeEditorFragment
import com.ordolabs.feature_home.util.FeatureHomeUtil.featureHomeComponent
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeEditorViewModel
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeObtainViewModel
import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.model.color.data.ColorSchemeRequest
import com.ordolabs.thecolor.util.ext.parentViewModels
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.getOrNull
import kotlinx.coroutines.flow.Flow
import com.ordolabs.thecolor.R as RApp

class ColorSchemeObtainFragment :
    ColorDataObtainFragment<ColorScheme>() {

    private val schemeObtainVM: ColorSchemeObtainViewModel by viewModels {
        featureHomeComponent.viewModelFactory
    }
    private val schemeEditorVM: ColorSchemeEditorViewModel by parentViewModels {
        featureHomeComponent.viewModelFactory
    }

    override fun collectViewModelsData() {
        super.collectViewModelsData()
        collectDispatchConfigCommand()
    }

    override fun setViews() {
        super.setViews()
        view?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = resources.getDimensionPixelSize(RApp.dimen.offset_16)
        }
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