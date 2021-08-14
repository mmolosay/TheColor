package com.ordolabs.thecolor.ui.fragment.colorinput

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.domain.model.ColorModel
import com.ordolabs.thecolor.R
import com.ordolabs.thecolor.databinding.FragmentColorInputBinding
import com.ordolabs.thecolor.ui.fragment.BaseFragment
import com.ordolabs.thecolor.util.getFromEnumOrNull
import com.ordolabs.thecolor.util.replaceFragment
import com.ordolabs.thecolor.util.setFragment
import com.ordolabs.thecolor.viewmodel.ColorInputViewModel

class ColorInputFragment : BaseFragment(R.layout.fragment_color_input) {

    private val binding: FragmentColorInputBinding by viewBinding()
    private val colorInputVM: ColorInputViewModel by viewModels()

    private val initialColorInputModel = ColorModel.HEX

    override fun setUp() {

    }

    override fun setViews() {
        setDropdown()
        setInitialColorInputFragment()
    }

    private fun setDropdown() {
        val autoCompleteView = binding.inputDropdown.editText as? AutoCompleteTextView ?: return
        val typed = resources.obtainTypedArray(R.array.color_input_dropdown_items)
        val items = colorInputVM.getDropdownItems(typed) ?: return
        val adapter = ArrayAdapter(requireContext(), R.layout.item_color_input_dropdown, items)
        autoCompleteView.setAdapter(adapter)
        autoCompleteView.setOnItemClickListener(::onDropdownItemClick)
    }

    private fun setInitialColorInputFragment() {
        setFragment(getColorInputModelFragment(initialColorInputModel))
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onDropdownItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val model = getFromEnumOrNull<ColorModel>(position) ?: return
        val fragment = getColorInputModelFragment(model)
        replaceFragment(fragment)
    }

    private fun getColorInputModelFragment(model: ColorModel) =
        when (model) {
            ColorModel.HEX -> ColorInputHexFragment.newInstance()
            ColorModel.RGB -> ColorInputRgbFragment.newInstance()
            else -> error("You forgot to add new branches, dummy")
        }

    companion object {
        fun newInstance() = ColorInputFragment()
    }
}