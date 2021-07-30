package com.ordolabs.thecolor.ui.fragment

import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.thecolor.R
import com.ordolabs.thecolor.databinding.FragmentColorInputBinding
import com.ordolabs.thecolor.viewmodel.ColorInputViewModel

class ColorInputFragment : BaseFragment(R.layout.fragment_color_input) {

    private val binding: FragmentColorInputBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by viewModels()

    override fun setUp() {

    }

    override fun setViews() {
        setDropdown()
    }

    private fun setDropdown() {
        val typed = resources.obtainTypedArray(R.array.color_input_dropdown_items)
        val items = colorInputVM.getDropdownItems(typed)
        val adapter = ArrayAdapter(requireContext(), R.layout.item_color_input_dropdown, items)
        (binding.inputDropdown.editText as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    companion object {
        fun newInstance() = ColorInputFragment()
    }
}