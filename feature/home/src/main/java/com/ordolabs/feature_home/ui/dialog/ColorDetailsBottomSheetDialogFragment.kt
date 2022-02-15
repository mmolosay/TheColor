package com.ordolabs.feature_home.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.ui.fragment.color.data.details.ColorDetailsObtainFragment
import com.ordolabs.feature_home.ui.fragment.color.data.details.ColorDetailsParent
import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.model.color.data.ColorDetails
import com.ordolabs.thecolor.ui.dialog.BaseBottomSheetDialogFragment
import com.ordolabs.thecolor.util.ContextUtil
import com.ordolabs.thecolor.util.ext.getDefaultTransactionTag

class ColorDetailsBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment(),
    ColorDetailsParent {

    private var details: ColorDetails? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.color_data_details_dialog, container, false)
    }

    // region Parse arguments

    override fun parseArguments(args: Bundle) {
        super.parseArguments(args)
        parseColorDetails(args)
    }

    private fun parseColorDetails(bundle: Bundle) {
        val key = ARGUMENT_KEY_COLOR_DETAILS
        if (!bundle.containsKey(key)) return
        this.details = bundle.getParcelable(key)
    }

    // endregion

    // region Set fragments

    override fun setFragments() {
        super.setFragments()
        setColorDetailsFragment()
    }

    private fun setColorDetailsFragment() {
        val fragment = ColorDetailsObtainFragment.newInstance(details)
        val tag = fragment.getDefaultTransactionTag()
        ContextUtil.setFragment(
            childFragmentManager,
            fragment,
            R.id.defaultFragmentContainer,
            tag
        )
    }

    // endregion

    // region Set views

    override fun setViews() {
        // nothing is here
    }

    // endregion

    // region ColorDetailsParent

    override fun onExactColorClick(exact: Color) {
        TODO()
    }

    // endregion

    companion object {

        private const val ARGUMENT_KEY_COLOR_DETAILS = "ARGUMENT_KEY_COLOR_DETAILS"

        fun newInstance(details: ColorDetails) =
            ColorDetailsBottomSheetDialogFragment().apply {
                arguments = bundleOf(
                    ARGUMENT_KEY_COLOR_DETAILS to details
                )
            }
    }
}