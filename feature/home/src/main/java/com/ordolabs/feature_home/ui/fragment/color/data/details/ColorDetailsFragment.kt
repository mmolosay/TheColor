package com.ordolabs.feature_home.ui.fragment.color.data.details

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorDetailsFragmentBinding
import com.ordolabs.feature_home.ui.fragment.color.data.base.BaseColorDataFragment
import com.ordolabs.feature_home.util.FeatureHomeUtil.featureHomeComponent
import com.ordolabs.feature_home.viewmodel.colordata.details.ColorDetailsViewModel
import com.ordolabs.thecolor.model.color.data.ColorDetails
import com.ordolabs.thecolor.util.InflaterUtil.cloneInViewContext
import com.ordolabs.thecolor.util.ext.getStringYesOrNo
import com.ordolabs.thecolor.util.ext.makeArgumentsKey
import com.ordolabs.thecolor.util.ext.setTextOrGoneWith

/**
 * [BaseColorDataFragment] that displays [ColorDetails] data.
 */
class ColorDetailsFragment :
    BaseColorDataFragment<ColorDetails>() {

    private val binding: ColorDetailsFragmentBinding by viewBinding(CreateMethod.BIND)
    private val colorDetailsVM: ColorDetailsViewModel by activityViewModels {
        featureHomeComponent.viewModelFactory
    }

    private var colorDetails: ColorDetails? = null

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
        return inflater
            .cloneInViewContext(container)
            .inflate(R.layout.color_details_fragment, container, false)
    }

    // region Parse arguments

    private fun parseArguments() {
        val args = arguments ?: return
        parseColorDetails(args)
    }

    private fun parseColorDetails(bundle: Bundle) {
        val key = ARGUMENT_KEY_COLOR_DETAILS
        if (!bundle.containsKey(key)) return
        this.colorDetails = bundle.getParcelable(key)
    }

    // endregion

    // region Set up

    override fun collectViewModelsData() {
        // nothing is here
    }

    // endregion

    // region Set views

    override fun setViews() {
        colorDetails?.let { details ->
            populateViews(details)
        }
    }

    // endregion

    // region IColorDataFragment

    override fun populateViews(data: ColorDetails) {
        populateNameHeadline(data)
        populateHexGroup(data)
        populateRgbGroup(data)
        populateHslGroup(data)
        populateHsvGroup(data)
        populateCmykGroup(data)
        populateNameGroup(data)
        populateMatchGroup(data)
    }

    private fun populateNameHeadline(details: ColorDetails) =
        binding.run {
            nameHeadline.text = details.name
        }

    private fun populateHexGroup(details: ColorDetails) =
        binding.run {
            val hasData = (details.hexValue != null)
            hexGroup.isVisible = hasData
            if (!hasData) return
            hexValue.text = details.hexValue
        }

    private fun populateRgbGroup(details: ColorDetails) =
        binding.run {
            val hasData = (details.rgbR != null && details.rgbG != null && details.rgbB != null)
            rgbGroup.isVisible = hasData
            if (!hasData) return
            rgbValueR.text = details.rgbR.toString()
            rgbValueG.text = details.rgbG.toString()
            rgbValueB.text = details.rgbB.toString()
        }

    private fun populateHslGroup(details: ColorDetails) =
        binding.run {
            val hasData = (details.hslH != null && details.hslS != null && details.hslL != null)
            hslGroup.isVisible = hasData
            if (!hasData) return
            hslValueH.text = details.hslH.toString()
            hslValueS.text = details.hslS.toString()
            hslValueL.text = details.hslL.toString()
        }

    private fun populateHsvGroup(details: ColorDetails) =
        binding.run {
            val hasData = (details.hsvH != null && details.hsvS != null && details.hsvV != null)
            hsvGroup.isVisible = hasData
            if (!hasData) return
            hsvValueH.text = details.hsvH.toString()
            hsvValueS.text = details.hsvS.toString()
            hsvValueV.text = details.hsvV.toString()
        }

    private fun populateCmykGroup(details: ColorDetails) =
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

    private fun populateNameGroup(details: ColorDetails) =
        binding.run {
            val hasData = (details.name != null)
            nameGroup.isVisible = hasData
            if (!hasData) return
            nameValue.text = details.name
        }

    private fun populateMatchGroup(details: ColorDetails) =
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

    private fun populateExactGroup(details: ColorDetails) =
        binding.run {
            val hasData = (details.exactNameHexSigned != null)
            exactGroup.isVisible = hasData
            if (!hasData) return
            val exactSigned = details.exactNameHexSigned
            val color = Color.parseColor(details.exactNameHexSigned)
            exactValue.setTextOrGoneWith(exactSigned, exactGroup)
            exactColor.backgroundTintList = ColorStateList.valueOf(color)
            exactLink.setOnClickListener {
                val exactHex = details.exactNameHex ?: return@setOnClickListener
                colorDetailsVM.getExactColor(exactHex)
            }
        }

    private fun populateDeviationGroup(details: ColorDetails) =
        binding.run {
            val hasData = (details.exactNameHexDistance != null)
            deviationGroup.isVisible = hasData
            if (!hasData) return
            deviationValue.text = details.exactNameHexDistance.toString()
        }

    // endregion

    companion object {

        private val ARGUMENT_KEY_COLOR_DETAILS =
            "ARGUMENT_KEY_COLOR_DETAILS".makeArgumentsKey<ColorDetailsFragment>()

        fun newInstance(colorDetails: ColorDetails?) =
            ColorDetailsFragment().apply {
                arguments = bundleOf(
                    ARGUMENT_KEY_COLOR_DETAILS to colorDetails
                )
            }
    }
}