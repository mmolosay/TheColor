package com.ordolabs.feature_home.ui.fragment

import androidx.core.view.isInvisible
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.runCatching
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentColorInformationBinding
import com.ordolabs.feature_home.viewmodel.ColorInformationViewModel
import com.ordolabs.feature_home.viewmodel.ColorInputViewModel
import com.ordolabs.thecolor.model.ColorInformationPresentation
import com.ordolabs.thecolor.util.ext.showToast
import com.ordolabs.thecolor.util.struct.getOrNull
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ColorInformationFragment : BaseFragment(R.layout.fragment_color_information) {

    private val binding: FragmentColorInformationBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()
    private val colorInfoVM: ColorInformationViewModel by sharedViewModel()

    override fun collectViewModelsData() {
        collectColorPreview()
        collectProcceedCommand()
        collectColorInformation()
        collectCoroutineException()
    }

    override fun setViews() {
        // nothing is here
    }

    private fun populateInformationViews(info: ColorInformationPresentation) =
        binding.run {
            name.text = info.name
        }

    private fun toggleVisibility(visible: Boolean) {
        view?.isInvisible = !visible
    }

    private fun collectColorPreview() =
        colorInputVM.colorPreview.collectOnLifecycle { resource ->
            val infoHex = colorInfoVM.information.value.getOrNull()?.hexValue
            val previewHex = resource.getOrNull()?.hexWithNumberSign
            if (infoHex == previewHex) return@collectOnLifecycle
            colorInfoVM.clearColorInformation()
            toggleVisibility(visible = false)
        }

    private fun collectProcceedCommand() =
        colorInputVM.procceedCommand.collectOnLifecycle { resource ->
            resource.ifSuccess { color ->
                colorInfoVM.fetchColorInformation(color)
            }
        }

    private fun collectColorInformation() =
        colorInfoVM.information.collectOnLifecycle { resource ->
            resource.ifSuccess { information ->
                toggleVisibility(visible = true)
                populateInformationViews(information)
            }
        }

    private fun collectCoroutineException() =
        colorInfoVM.coroutineExceptionMessageRes.collectOnLifecycle { idres ->
            val text = Result.runCatching { getString(idres) }.get() ?: return@collectOnLifecycle
            showToast(text)
        }

    companion object {

        fun newInstance() = ColorInformationFragment()
    }
}