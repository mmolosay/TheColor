package com.ordolabs.feature_home.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.ui.fragment.color.data.details.ColorDetailsFragment
import com.ordolabs.thecolor.model.color.data.ColorDetails
import com.ordolabs.thecolor.ui.dialog.BaseBottomSheetDialogFragment
import com.ordolabs.thecolor.util.ContextUtil
import com.ordolabs.thecolor.util.ext.getDefaultTransactionTag
import com.ordolabs.thecolor.util.ext.makeArgumentsKey

class ColorDetailsBottomSheetDialogFragment : BaseBottomSheetDialogFragment() {

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
        val fragment = ColorDetailsFragment.newInstance(details)
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

    companion object {

        private val ARGUMENT_KEY_COLOR_DETAILS =
            "ARGUMENT_COLOR_DETAILS".makeArgumentsKey<ColorDetailsBottomSheetDialogFragment>()

        fun newInstance(details: ColorDetails) =
            ColorDetailsBottomSheetDialogFragment().apply {
                arguments = bundleOf(
                    ARGUMENT_KEY_COLOR_DETAILS to details
                )
            }
    }
}