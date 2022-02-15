package com.ordolabs.feature_home.ui.fragment.color.data.scheme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorSchemeFragmentBinding
import com.ordolabs.feature_home.ui.adapter.recycler.ColorSchemeSamplesAdapter
import com.ordolabs.feature_home.ui.dialog.ColorDetailsBottomSheetDialogFragment
import com.ordolabs.feature_home.ui.fragment.color.data.base.BaseColorDataFragment
import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.ui.adapter.base.OnRecyclerItemClicksListener
import com.ordolabs.thecolor.ui.util.itemdecoration.OverlapingDecoration
import com.ordolabs.thecolor.util.InflaterUtil.cloneInViewContext

/**
 * [BaseColorDataFragment] that displays [ColorScheme] data.
 */
class ColorSchemeFragment :
    BaseColorDataFragment<ColorScheme>(),
    OnRecyclerItemClicksListener {

    private val binding: ColorSchemeFragmentBinding by viewBinding(CreateMethod.BIND)

    private val samplesAdapter =
        ColorSchemeSamplesAdapter().also {
            it.setOnClicksListener(this)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inherit container view group theme
        return inflater.cloneInViewContext(container)
            .inflate(R.layout.color_scheme_fragment, container, false)
    }

    override fun collectViewModelsData() {
        // impl me
    }

    override fun setViews() {
        setSamplesRecycler()
    }

    private fun setSamplesRecycler() {
        val recycler = binding.samples
        val offset = resources.getDimensionPixelOffset(R.dimen.color_scheme_sample_size) / 2
        val decoration = OverlapingDecoration.Horizontal(offset)
        recycler.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        recycler.adapter = samplesAdapter
        recycler.addItemDecoration(decoration)
    }

    // region ColorDataView

    override fun populateViews(data: ColorScheme) =
        binding.root.doOnLayout {
            data.samples?.let {
                samplesAdapter.setItems(it)
            }
        }

    // endregion

    // region OnRecyclerItemClicksListener

    override fun onRecyclerItemClick(position: Int) {
        super.onRecyclerItemClick(position)
        val sample = samplesAdapter.samples.getOrNull(position) ?: return
        val details = sample.details
        ColorDetailsBottomSheetDialogFragment
            .newInstance(details)
            .show(childFragmentManager)
    }

    override fun onRecyclerItemLongClick(position: Int) {
        super.onRecyclerItemLongClick(position)
    }

    // endregion

    companion object {
        fun newInstance() =
            ColorSchemeFragment()
    }
}