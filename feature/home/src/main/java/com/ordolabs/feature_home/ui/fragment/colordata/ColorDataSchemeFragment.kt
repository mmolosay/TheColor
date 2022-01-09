package com.ordolabs.feature_home.ui.fragment.colordata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorDataSchemeFragmentBinding

class ColorDataSchemeFragment : BaseColorDataFragment<Unit /* TODO: type */>() {

    private val binding: ColorDataSchemeFragmentBinding by viewBinding()

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
        return fInflater.inflate(R.layout.color_data_scheme_fragment, container, false)
    }

    override fun collectViewModelsData() {
        // impl me
    }

    override fun setViews() {
        // impl me
    }

    override fun populateViews(data: Unit) {
        // TODO: impl me
    }

    companion object {
        fun newInstance() =
            ColorDataSchemeFragment()
    }
}