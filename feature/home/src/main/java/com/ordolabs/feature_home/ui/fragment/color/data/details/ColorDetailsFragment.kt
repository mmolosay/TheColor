package com.ordolabs.feature_home.ui.fragment.color.data.details

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorDetailsFragmentBinding
import com.ordolabs.feature_home.ui.fragment.color.data.base.BaseColorDataFragment
import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.model.color.data.ColorDetails
import com.ordolabs.thecolor.model.color.toColorInt
import com.ordolabs.thecolor.util.InflaterUtil.cloneInViewContext
import com.ordolabs.thecolor.util.ext.activityFragmentManager
import com.ordolabs.thecolor.util.ext.getStringYesOrNo
import com.ordolabs.thecolor.util.ext.setTextOrGone
import com.ordolabs.thecolor.util.ext.setTextOrGoneWith

/**
 * [BaseColorDataFragment] that displays [ColorDetails] data.
 */
class ColorDetailsFragment :
    BaseColorDataFragment<ColorDetails>() {

    private val binding: ColorDetailsFragmentBinding by viewBinding(CreateMethod.BIND)

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
        populateNameHeadline(data.exact.name)
        populateHexGroup(data.spaces.hex)
        populateRgbGroup(data.spaces.rgb)
        populateHslGroup(data.spaces.hsl)
        populateHsvGroup(data.spaces.hsv)
        populateCmykGroup(data.spaces.cmyk)
        populateNameGroup(data.exact)
        populateMatchGroup(data.exact)
    }

    private fun populateNameHeadline(name: String?) =
        binding.run {
            nameHeadline.setTextOrGone(name)
        }

    private fun populateHexGroup(hex: ColorDetails.Spaces.Hex) =
        binding.run {
            val hasData = (hex.signless != null)
            hexGroup.isVisible = hasData
            if (!hasData) return
            hexValue.text = hex.signed
        }

    private fun populateRgbGroup(rgb: ColorDetails.Spaces.Rgb) =
        binding.run {
            val hasData = (rgb.r != null && rgb.g != null && rgb.b != null)
            rgbGroup.isVisible = hasData
            if (!hasData) return
            rgbValueR.text = rgb.r.toString()
            rgbValueG.text = rgb.g.toString()
            rgbValueB.text = rgb.b.toString()
        }

    private fun populateHslGroup(hsl: ColorDetails.Spaces.Hsl) =
        binding.run {
            val hasData = (hsl.h != null && hsl.s != null && hsl.l != null)
            hslGroup.isVisible = hasData
            if (!hasData) return
            hslValueH.text = hsl.h.toString()
            hslValueS.text = hsl.s.toString()
            hslValueL.text = hsl.l.toString()
        }

    private fun populateHsvGroup(hsv: ColorDetails.Spaces.Hsv) =
        binding.run {
            val hasData = (hsv.h != null && hsv.s != null && hsv.v != null)
            hsvGroup.isVisible = hasData
            if (!hasData) return
            hsvValueH.text = hsv.h.toString()
            hsvValueS.text = hsv.s.toString()
            hsvValueV.text = hsv.v.toString()
        }

    private fun populateCmykGroup(cmyk: ColorDetails.Spaces.Cmyk) =
        binding.run {
            val hasData =
                (cmyk.c != null && cmyk.m != null && cmyk.y != null && cmyk.k != null)
            cmykGroup.isVisible = hasData
            if (!hasData) return
            cmykValueC.text = cmyk.c.toString()
            cmykValueM.text = cmyk.m.toString()
            cmykValueY.text = cmyk.y.toString()
            cmykValueK.text = cmyk.k.toString()
        }

    private fun populateNameGroup(exact: ColorDetails.Exact) =
        binding.run {
            val hasData = (exact.name != null)
            nameGroup.isVisible = hasData
            if (!hasData) return
            nameValue.text = exact.name
        }

    private fun populateMatchGroup(exact: ColorDetails.Exact) =
        binding.run {
            val hasData = (exact.isMatch != null)
            val exactMatch = (exact.isMatch == true)
            matchGroup.isVisible = hasData
            if (!hasData) return
            matchValue.text = resources.getStringYesOrNo(yes = exactMatch)
            matchGroups.isVisible = !exactMatch
            if (exactMatch) return
            populateExactGroup(exact)
            populateDeviationGroup(exact)
        }

    private fun populateExactGroup(exact: ColorDetails.Exact) =
        binding.run {
            val hasData = (exact.color != null)
            exactGroup.isVisible = hasData
            if (!hasData) return
            val color = exact.color!! // checked above
            exactValue.setTextOrGoneWith(color.hex, exactGroup)
            exactColor.backgroundTintList = ColorStateList.valueOf(color.toColorInt())
            exactLink.setOnClickListener {
                activityFragmentManager.setFragmentResult(
                    RESULT_KEY_EXACT_COLOR_COMMAND,
                    makeResultBundle(color)
                )
            }
        }

    private fun populateDeviationGroup(exact: ColorDetails.Exact) =
        binding.run {
            val hasData = (exact.distance != null)
            deviationGroup.isVisible = hasData
            if (!hasData) return
            deviationValue.text = exact.distance.toString()
        }

    // endregion

    data class Result(
        val exactColor: Color?
    )

    companion object {

        const val RESULT_KEY_EXACT_COLOR_COMMAND = "RESULT_KEY_EXACT_COLOR_COMMAND"

        private const val ARGUMENT_KEY_COLOR_DETAILS = "ARGUMENT_KEY_COLOR_DETAILS"

        fun newInstance(colorDetails: ColorDetails?) =
            ColorDetailsFragment().apply {
                arguments = bundleOf(
                    ARGUMENT_KEY_COLOR_DETAILS to colorDetails
                )
            }

        // region Fragment Result

        fun makeResultBundle(
            exactColor: Color
        ) =
            bundleOf(
                "exactColor" to exactColor
            )

        fun parseResultBundle(bundle: Bundle) =
            Result(
                exactColor = bundle.getParcelable("exactColor")
            )

        // endregion
    }
}