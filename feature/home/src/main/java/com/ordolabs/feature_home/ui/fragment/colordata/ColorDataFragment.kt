package com.ordolabs.feature_home.ui.fragment.colordata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorDataFragmentBinding
import com.ordolabs.feature_home.ui.adapter.pager.ColorDataPagerAdapter
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.thecolor.util.ColorUtil
import com.ordolabs.thecolor.util.ext.makeArgumentsKey

open class ColorDataFragment : BaseFragment() {

    private val binding: ColorDataFragmentBinding by viewBinding(CreateMethod.BIND)

    // TODO: implement trivial no content view, if color is null somehow
    protected var color: ColorUtil.Color? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.color_data_fragment, container, false)
    }

    override fun parseArguments() {
        parseColorArg()
    }

    private fun parseColorArg() {
        val key = ARGUMENTS_KEY_COLOR
        val args = arguments ?: return
        if (!args.containsKey(key)) return
        this.color = args.getParcelable(key)
    }

    override fun collectViewModelsData() {
        // nothing is here
    }

    override fun setViews() {
        setViewPager()
    }

    private fun setViewPager() =
        binding.pager.let { pager ->
            val adapter = ColorDataPagerAdapter(this)
            pager.adapter = adapter
        }

    companion object {

        private val ARGUMENTS_KEY_COLOR =
            "ARGUMENTS_KEY_COLOR".makeArgumentsKey<ColorDataFragment>()

        fun newInstance(color: ColorUtil.Color?) =
            ColorDataFragment().apply {
                arguments = bundleOf(
                    ARGUMENTS_KEY_COLOR to color
                )
            }
    }
}