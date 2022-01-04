package com.ordolabs.feature_home.ui.fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.runCatching
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.FragmentColorInformationBinding
import com.ordolabs.feature_home.viewmodel.ColorInformationViewModel
import com.ordolabs.feature_home.viewmodel.ColorInputViewModel
import com.ordolabs.thecolor.model.ColorInformationPresentation
import com.ordolabs.thecolor.util.ColorUtil
import com.ordolabs.thecolor.util.ColorUtil.isDark
import com.ordolabs.thecolor.util.ext.by
import com.ordolabs.thecolor.util.ext.getStringYesOrNo
import com.ordolabs.thecolor.util.ext.mediumAnimDuration
import com.ordolabs.thecolor.util.ext.setTextOrGoneWith
import com.ordolabs.thecolor.util.ext.showToast
import com.ordolabs.thecolor.util.struct.getOrNull
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.net.UnknownHostException
import com.ordolabs.thecolor.R as RApp

class ColorInformationFragment : BaseFragment() {

    private val binding: FragmentColorInformationBinding by viewBinding(CreateMethod.BIND)
    private val colorInputVM: ColorInputViewModel by sharedViewModel()
    private val colorInfoVM: ColorInformationViewModel by sharedViewModel()

    private val color: ColorUtil.Color? by lazy {
        colorInputVM.colorPreview.value.getOrNull()?.color
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        colorInfoVM.fetchColorInformation(color ?: return)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val themeOverlay = if (color?.isDark() == true) {
            RApp.style.ThemeOverlay_TheColor_Dark
        } else {
            RApp.style.ThemeOverlay_TheColor_Light
        }
        val themedContext = ContextThemeWrapper(activity, themeOverlay)
        return inflater.cloneInContext(themedContext)
            .inflate(R.layout.fragment_color_information, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        colorInfoVM.clearColorInformation()
    }

    override fun collectViewModelsData() {
        collectColorPreview()
        collectColorInformation()
        collectCoroutineException()
    }

    override fun setViews() {
        setNoContentView()
    }

    private fun setNoContentView() {
        setRetryBtn()
    }

    private fun setRetryBtn() {
        binding.noContent.retryBtn.setOnClickListener l@{
            colorInfoVM.fetchColorInformation(color ?: return@l)
        }
    }

    private fun showContentView() {
        binding.content.isVisible = true
        binding.contentShimmer.root.isVisible = false
        binding.noContent.root.isVisible = false
    }

    private fun showLoadingView() {
        binding.content.isVisible = false
        binding.contentShimmer.root.isVisible = true
        binding.noContent.root.isVisible = false
    }

    private fun showNoContentView() {
        binding.content.isVisible = false
        binding.contentShimmer.root.isVisible = false
        binding.noContent.root.isVisible = true
    }

    private fun populateInformationViews(info: ColorInformationPresentation) =
        binding.run {
            nameHeadline.text = info.name
            populateHexGroup(info)
            populateRgbGroup(info)
            populateHslGroup(info)
            populateHsvGroup(info)
            populateCmykGroup(info)
            populateNameGroup(info)
            populateMatchGroup(info)
        }

    private fun populateHexGroup(info: ColorInformationPresentation) =
        binding.run {
            val hasData = (info.hexValue != null)
            hexGroup.isVisible = hasData
            if (!hasData) return
            hexValue.text = info.hexValue
        }

    private fun populateRgbGroup(info: ColorInformationPresentation) =
        binding.run {
            val hasData = (info.rgbR != null && info.rgbG != null && info.rgbB != null)
            rgbGroup.isVisible = hasData
            if (!hasData) return
            rgbValueR.text = info.rgbR.toString()
            rgbValueG.text = info.rgbG.toString()
            rgbValueB.text = info.rgbB.toString()
        }

    private fun populateHslGroup(info: ColorInformationPresentation) =
        binding.run {
            val hasData = (info.hslH != null && info.hslS != null && info.hslL != null)
            hslGroup.isVisible = hasData
            if (!hasData) return
            hslValueH.text = info.hslH.toString()
            hslValueS.text = info.hslS.toString()
            hslValueL.text = info.hslL.toString()
        }

    private fun populateHsvGroup(info: ColorInformationPresentation) =
        binding.run {
            val hasData = (info.hsvH != null && info.hsvS != null && info.hsvV != null)
            hsvGroup.isVisible = hasData
            if (!hasData) return
            hsvValueH.text = info.hsvH.toString()
            hsvValueS.text = info.hsvS.toString()
            hsvValueV.text = info.hsvV.toString()
        }

    private fun populateCmykGroup(info: ColorInformationPresentation) =
        binding.run {
            val hasData =
                (info.cmykC != null && info.cmykM != null && info.cmykY != null && info.cmykK != null)
            cmykGroup.isVisible = hasData
            if (!hasData) return
            cmykValueC.text = info.cmykC.toString()
            cmykValueM.text = info.cmykM.toString()
            cmykValueY.text = info.cmykY.toString()
            cmykValueK.text = info.cmykK.toString()
        }

    private fun populateNameGroup(info: ColorInformationPresentation) =
        binding.run {
            val hasData = (info.name != null)
            nameGroup.isVisible = hasData
            if (!hasData) return
            nameValue.text = info.name
        }

    private fun populateMatchGroup(info: ColorInformationPresentation) =
        binding.run {
            val hasData = (info.isNameMatchExact != null)
            val exactMatch = (info.isNameMatchExact == true)
            matchGroup.isVisible = hasData
            if (!hasData) return
            matchValue.text = resources.getStringYesOrNo(yes = exactMatch)
            matchGroups.isVisible = !exactMatch
            if (exactMatch) return
            populateExactGroup(info)
            populateDeviationGroup(info)
        }

    private fun populateExactGroup(info: ColorInformationPresentation) =
        binding.run {
            val hasData = (info.exactNameHex != null)
            exactGroup.isVisible = hasData
            if (!hasData) return
            val exact = info.exactNameHex
            val color = Color.parseColor(info.exactNameHex)
            exactValue.setTextOrGoneWith(exact, exactGroup)
            exactColor.backgroundTintList = ColorStateList.valueOf(color)
            exactLink.setOnClickListener {
                exact ?: return@setOnClickListener
                val exactColor = ColorUtil.Color(hex = exact)
                colorInfoVM.fetchColorInformation(exactColor)
            }
        }

    private fun populateDeviationGroup(info: ColorInformationPresentation) =
        binding.run {
            val hasData = (info.exactNameHexDistance != null)
            deviationGroup.isVisible = hasData
            if (!hasData) return
            deviationValue.text = info.exactNameHexDistance.toString()
        }

    private fun animContentVisibility(visible: Boolean, instant: Boolean = false) {
        val content = binding.content
        val translation = resources.getDimension(RApp.dimen.offset_8)
        val translationY = 0f to translation by visible
        val alpha = 1f to 0f by visible
        val duration = 0L to mediumAnimDuration by instant
        ViewCompat.animate(content)
            .translationY(translationY)
            .alpha(alpha)
            .setDuration(duration)
            .setInterpolator(FastOutSlowInInterpolator())
            .withStartAction {
                showContentView()
            }
            .withEndAction {
                content.isVisible = visible
            }
            .start()
    }

    private fun collectColorPreview() =
        colorInputVM.colorPreview.collectOnLifecycle { resource ->
            resource.ifSuccess { colorPreview ->
                val info = colorInfoVM.information.value.getOrNull() ?: return@ifSuccess
                if (!colorPreview.color.equals(info.hexValue)) { // info for another color
                    colorInfoVM.clearColorInformation()
                }
            }
        }

    private fun collectColorInformation() =
        colorInfoVM.information.collectOnLifecycle { resource ->
            resource.fold(
                onEmpty = ::onColorInformationEmpty,
                onLoading = ::onColorInformationLoading,
                onSuccess = ::onColorInformationSuccess,
                onFailure = ::onColorInformationFailure
            )
        }

    @Suppress("UNUSED_PARAMETER")
    private fun onColorInformationEmpty(previous: ColorInformationPresentation?) {
        animContentVisibility(visible = false)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onColorInformationLoading(previous: ColorInformationPresentation?) {
        showLoadingView()
//        if (previous != null) {
//            animContentVisibility(visible = false, instant = true)
//        }
    }

    private fun onColorInformationSuccess(info: ColorInformationPresentation) {
        info.hexClean?.let {
            // update colorPreview if exact color was fetched
            val preview = colorInputVM.colorPreview.value.getOrNull()
            if (preview?.color?.hex == it) return@let
            val color = ColorUtil.Color(hex = it)
            val new = ColorInputViewModel.ColorPreview(color, isUserInput = false)
            colorInputVM.updateColorPreview(new)
        }

        populateInformationViews(info)
        animContentVisibility(visible = true)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onColorInformationFailure(
        previous: ColorInformationPresentation?,
        payload: Any?,
        error: Throwable
    ) =
        when (error) {
            is UnknownHostException -> showNoContentView()
            else -> showToast(error.localizedMessage)
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