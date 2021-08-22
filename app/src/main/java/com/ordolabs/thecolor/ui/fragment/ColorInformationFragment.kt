package com.ordolabs.thecolor.ui.fragment

import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.thecolor.R
import com.ordolabs.thecolor.databinding.FragmentColorInformationBinding
import com.ordolabs.thecolor.model.ColorInformationPresentation
import com.ordolabs.thecolor.viewmodel.ColorInformationViewModel
import com.ordolabs.thecolor.viewmodel.ColorInputViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ColorInformationFragment : BaseFragment(R.layout.fragment_color_information) {

    private val binding: FragmentColorInformationBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by sharedViewModel()
    private val colorInformationVM: ColorInformationViewModel by viewModel()

    override fun setUp() {
        observeProcceedCommand()
        observeColorInformation()
    }

    override fun setViews() {
        // nothing is here
    }

    private fun populateInformationViews(info: ColorInformationPresentation) = binding.run {
        name.text = info.name
    }

    private fun observeProcceedCommand() =
        colorInputVM.getProcceedCommand().observe(this) { color ->
            colorInformationVM.fetchColorInformation(color)
        }

    private fun observeColorInformation() =
        colorInformationVM.getColorInformation().observe(this) { resource ->
            resource.ifSuccess { information ->
                populateInformationViews(information)
            }
        }

    companion object {

        fun newInstance() = ColorInformationFragment()
    }
}