package com.ordolabs.thecolor.ui.adapter.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

/**
 * An abstract implementation of [ViewHolder]
 * to be used in [BaseAdapter].
 *
 * @param T Type of data class, which this `ViewHolder` represents.
 */
abstract class BaseViewHolder<T : Any>(itemView: View) :
    RecyclerView.ViewHolder(itemView),
    View.OnClickListener,
    View.OnLongClickListener {

    /**
     * Returns [ViewHolder.itemView]'s [View.getTag],
     * casted to [T] for more convenience.
     */
    @Suppress("UNCHECKED_CAST")
    protected val boundItem: T; get() = this.itemView.tag as T

    /**
     * Binds specified [item] data with ViewHolder's views.
     * Should be called in [BaseAdapter.onBindViewHolder]
     * to bind data with [ViewHolder.itemView].
     */
    fun onBind(item : T) {
        this.itemView.tag = item
        populate(item)
    }

    /**
     * Configures [itemView]'s views in [onBind].
     */
    protected abstract fun populate(item: T)

    override fun onClick(v: View?) {
        // default empty implementation
    }

    override fun onLongClick(v: View?): Boolean {
        // default empty implementation
        return false
    }
}