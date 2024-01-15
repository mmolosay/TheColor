package io.github.mmolosay.thecolor.presentation.ui.adapter.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

/**
 * An abstract implementation of [ViewHolder] to be used in [BaseAdapter].
 *
 * @param T Type of data class, which this `ViewHolder` represents.
 */
abstract class BaseViewHolder<T : Any>(itemView: View) :
    RecyclerView.ViewHolder(itemView),
    View.OnClickListener,
    View.OnLongClickListener {

    /**
     * Populates [itemView] with [item] data.
     * Should be called in [BaseAdapter.onBindViewHolder] to bind data with [ViewHolder.itemView].
     */
    abstract fun populate(item: T)

    override fun onClick(v: View?) {
        // default empty implementation
    }

    override fun onLongClick(v: View?): Boolean {
        // default empty implementation
        return false
    }
}