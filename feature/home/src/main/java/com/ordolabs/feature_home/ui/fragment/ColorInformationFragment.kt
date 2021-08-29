package com.ordolabs.feature_home.ui.fragment

import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentColorInformationBinding
import com.ordolabs.feature_home.viewmodel.ColorInformationViewModel
import com.ordolabs.feature_home.viewmodel.ColorInputViewModel
import com.ordolabs.thecolor.model.ColorInformationPresentation
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ColorInformationFragment : BaseFragment(R.layout.fragment_color_information) {

    private val binding: FragmentColorInformationBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()
    private val colorInformationVM: ColorInformationViewModel by viewModel()

    override fun collectViewModelsData() {
        collectProcceedCommand()
        collectColorInformation()
    }

    override fun setViews() {
        // nothing is here
    }

    private fun populateInformationViews(info: ColorInformationPresentation) = binding.run {
        name.text = info.name
    }

    private fun collectColorInformation() =
        colorInformationVM.information.collectOnLifecycle { resource ->
            resource.ifSuccess { information ->
                populateInformationViews(information)
            }
        }

    private fun collectProcceedCommand() =
        colorInputVM.procceedCommand.collectOnLifecycle { resource ->
            resource.ifSuccess { color ->
                colorInformationVM.fetchColorInformation(color)
            }
        }

    companion object {

        fun newInstance() = ColorInformationFragment()
    }
}