package com.ordolabs.feature_home.ui.fragment.colordata.scheme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorSchemeFragmentBinding
import com.ordolabs.feature_home.ui.adapter.recycler.ColorSchemeSamplesAdapter
import com.ordolabs.feature_home.ui.fragment.colordata.base.BaseColorDataFragment
import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.ui.adapter.base.OnRecyclerItemClicksListener
import com.ordolabs.thecolor.util.InflaterUtil.cloneInViewContext

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
        recycler.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        recycler.adapter = samplesAdapter
    }

    // region IColorDataFragment

    override fun populateViews(data: ColorScheme) {
        data.samples?.let {
            samplesAdapter.setItems(it)
        }
    }

    // endregion

    // region OnRecyclerItemClicksListener

    override fun onRecyclerItemClick(position: Int) {
        super.onRecyclerItemClick(position)
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