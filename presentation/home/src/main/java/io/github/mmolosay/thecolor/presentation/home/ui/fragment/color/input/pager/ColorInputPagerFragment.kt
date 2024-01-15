package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.input.pager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.github.mmolosay.thecolor.presentation.home.R
import io.github.mmolosay.thecolor.presentation.home.databinding.ColorInputPagerFragmentBinding
import io.github.mmolosay.thecolor.presentation.home.ui.adapter.pager.ColorInputPagerAdapter
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.input.page.ColorInputParent
import io.github.mmolosay.thecolor.presentation.home.viewmodel.color.input.ColorInputViewModel
import io.github.mmolosay.thecolor.presentation.color.Color
import io.github.mmolosay.thecolor.presentation.color.ColorPrototype
import io.github.mmolosay.thecolor.presentation.ui.util.itemdecoration.MarginDecoration
import io.github.mmolosay.thecolor.presentation.util.ext.getFromEnumOrNull
import io.github.mmolosay.thecolor.presentation.util.ext.requireParentOf
import dagger.hilt.android.AndroidEntryPoint
import io.github.mmolosay.thecolor.presentation.fragment.BaseFragment

/**
 * `Fragment` with `ViewPager` which contains `Fragment`s of specific color scheme inputs.
 * Requires parent `Fragment` to be an instance of [ColorInputParent].
 */
@AndroidEntryPoint
class ColorInputPagerFragment :
    BaseFragment(),
    ColorInputPagerView,
    ColorInputParent {

    private val binding by viewBinding(ColorInputPagerFragmentBinding::bind)
    private val colorInputVM: ColorInputViewModel by viewModels()

    private val parent: ColorInputParent? by lazy { requireParentOf() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.color_input_pager_fragment, container, false)
    }

    // region Set views

    override fun setViews() {
        setViewPager()
        setTabs()
    }

    private fun setViewPager() = binding.run {
        val adapter =
            io.github.mmolosay.thecolor.presentation.home.ui.adapter.pager.ColorInputPagerAdapter(
                this@ColorInputPagerFragment
            )
        val decoration = MarginDecoration.Horizontal(
            resources,
            R.dimen.offset_content_horizontal
        )
        pager.adapter = adapter
        pager.offscreenPageLimit = adapter.itemCount
        pager.addItemDecoration(decoration)
    }

    private fun setTabs() = binding.run {
        TabLayoutMediator(tabs, pager, ::configureInputTab).attach()
    }

    private fun configureInputTab(tab: TabLayout.Tab, position: Int) {
        val page = getFromEnumOrNull<io.github.mmolosay.thecolor.presentation.home.ui.adapter.pager.ColorInputPagerAdapter.Page>(position) ?: return
        tab.setText(page.titleRes)
    }

    // endregion

    // region ColorInputPagerView

    override fun updateCurrentColor(color: Color) {
        colorInputVM.updateCurrentColor(color)
    }

    override fun clearCurrentColor() {
        colorInputVM.clearColorInput()
    }

    // endregion

    // region ColorInputParent

    // delegates
    override fun onInputChanged(input: ColorPrototype) {
        parent?.onInputChanged(input)
    }

    // endregion

    companion object {
        fun newInstance() = ColorInputPagerFragment()
    }
}