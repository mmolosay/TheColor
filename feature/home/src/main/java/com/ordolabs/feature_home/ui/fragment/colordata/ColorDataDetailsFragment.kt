package com.ordolabs.feature_home.ui.fragment.colordata

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorDataDetailsFragmentBinding
import com.ordolabs.feature_home.viewmodel.ColorDataViewModel
import com.ordolabs.feature_home.viewmodel.ColorInputViewModel
import com.ordolabs.thecolor.model.ColorDetailsPresentation
import com.ordolabs.thecolor.util.ColorUtil
import com.ordolabs.thecolor.util.ext.getStringYesOrNo
import com.ordolabs.thecolor.util.ext.makeArgumentsKey
import com.ordolabs.thecolor.util.ext.setTextOrGoneWith
import com.ordolabs.thecolor.util.struct.getOrNull
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ColorDataDetailsFragment : BaseColorDataFragment<ColorDetailsPresentation>() {

    private val binding: ColorDataDetailsFragmentBinding by viewBinding(CreateMethod.BIND)
    private val colorInputVM: ColorInputViewModel by sharedViewModel()
    private val colorInfoVM: ColorDataViewModel by sharedViewModel()

    private var colorDetails: ColorDetailsPresentation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArguments()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inherit container view group theme
        val fInflater = if (container != null) {
            inflater.cloneInContext(container.context)
        } else {
            inflater
        }
        return fInflater.inflate(R.layout.color_data_details_fragment, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        colorInfoVM.clearColorInformation()
    }

    private fun parseArguments() {
        parseColorDetailsArg()
    }

    private fun parseColorDetailsArg() {
        val key = ARGUMENT_KEY_COLOR_DETAILS
        val args = arguments ?: return
        if (!args.containsKey(key)) return
        this.colorDetails = args.getParcelable(key)
    }

    override fun collectViewModelsData() {
        collectColorPreview()
    }

    override fun setViews() {
        colorDetails?.let { details ->
            populateViews(details)
        }
    }

    override fun populateViews(data: ColorDetailsPresentation) =
        binding.run {
            nameHeadline.text = data.name
            populateHexGroup(data)
            populateRgbGroup(data)
            populateHslGroup(data)
            populateHsvGroup(data)
            populateCmykGroup(data)
            populateNameGroup(data)
            populateMatchGroup(data)
        }

    private fun populateHexGroup(details: ColorDetailsPresentation) =
        binding.run {
            val hasData = (details.hexValue != null)
            hexGroup.isVisible = hasData
            if (!hasData) return
            hexValue.text = details.hexValue
        }

    private fun populateRgbGroup(details: ColorDetailsPresentation) =
        binding.run {
            val hasData = (details.rgbR != null && details.rgbG != null && details.rgbB != null)
            rgbGroup.isVisible = hasData
            if (!hasData) return
            rgbValueR.text = details.rgbR.toString()
            rgbValueG.text = details.rgbG.toString()
            rgbValueB.text = details.rgbB.toString()
        }

    private fun populateHslGroup(details: ColorDetailsPresentation) =
        binding.run {
            val hasData = (details.hslH != null && details.hslS != null && details.hslL != null)
            hslGroup.isVisible = hasData
            if (!hasData) return
            hslValueH.text = details.hslH.toString()
            hslValueS.text = details.hslS.toString()
            hslValueL.text = details.hslL.toString()
        }

    private fun populateHsvGroup(details: ColorDetailsPresentation) =
        binding.run {
            val hasData = (details.hsvH != null && details.hsvS != null && details.hsvV != null)
            hsvGroup.isVisible = hasData
            if (!hasData) return
            hsvValueH.text = details.hsvH.toString()
            hsvValueS.text = details.hsvS.toString()
            hsvValueV.text = details.hsvV.toString()
        }

    private fun populateCmykGroup(details: ColorDetailsPresentation) =
        binding.run {
            val hasData =
                (details.cmykC != null && details.cmykM != null && details.cmykY != null && details.cmykK != null)
            cmykGroup.isVisible = hasData
            if (!hasData) return
            cmykValueC.text = details.cmykC.toString()
            cmykValueM.text = details.cmykM.toString()
            cmykValueY.text = details.cmykY.toString()
            cmykValueK.text = details.cmykK.toString()
        }

    private fun populateNameGroup(details: ColorDetailsPresentation) =
        binding.run {
            val hasData = (details.name != null)
            nameGroup.isVisible = hasData
            if (!hasData) return
            nameValue.text = details.name
        }

    private fun populateMatchGroup(details: ColorDetailsPresentation) =
        binding.run {
            val hasData = (details.isNameMatchExact != null)
            val exactMatch = (details.isNameMatchExact == true)
            matchGroup.isVisible = hasData
            if (!hasData) return
            matchValue.text = resources.getStringYesOrNo(yes = exactMatch)
            matchGroups.isVisible = !exactMatch
            if (exactMatch) return
            populateExactGroup(details)
            populateDeviationGroup(details)
        }

    private fun populateExactGroup(details: ColorDetailsPresentation) =
        binding.run {
            val hasData = (details.exactNameHex != null)
            exactGroup.isVisible = hasData
            if (!hasData) return
            val exact = details.exactNameHex
            val color = Color.parseColor(details.exactNameHex)
            exactValue.setTextOrGoneWith(exact, exactGroup)
            exactColor.backgroundTintList = ColorStateList.valueOf(color)
            exactLink.setOnClickListener {
                exact ?: return@setOnClickListener
                val exactColor = ColorUtil.Color(hex = exact)
                colorInfoVM.fetchColorDetails(exactColor)
            }
        }

    private fun populateDeviationGroup(details: ColorDetailsPresentation) =
        binding.run {
            val hasData = (details.exactNameHexDistance != null)
            deviationGroup.isVisible = hasData
            if (!hasData) return
            deviationValue.text = details.exactNameHexDistance.toString()
        }

    private fun collectColorPreview() =
        colorInputVM.colorPreview.collectOnLifecycle { resource ->
            resource.ifSuccess { colorPreview ->
                val info = colorInfoVM.details.value.getOrNull() ?: return@ifSuccess
                if (!colorPreview.color.equals(colorDetails?.hexValue)) { // info for another color
                    colorInfoVM.clearColorInformation()
                }
            }
        }

    companion object {

        private val ARGUMENT_KEY_COLOR_DETAILS =
            "ARGUMENT_KEY_COLOR_DETAILS".makeArgumentsKey<ColorDataDetailsFragment>()

        fun newInstance(colorDetails: ColorDetailsPresentation?) =
            ColorDataDetailsFragment().apply {
                arguments = bundleOf(
                    ARGUMENT_KEY_COLOR_DETAILS to colorDetails
                )
            }
    }
}