package io.github.mmolosay.presentation.ui.adapter.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

/**
 * An abstract implementation of [RecyclerView.Adapter].
 */
abstract class BaseAdapter<T : Any, VH : BaseViewHolder<T>> :
    RecyclerView.Adapter<VH>() {

    var clicksListener: OnRecyclerItemClicksListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val root = createItemView(parent, viewType)
        val holder = createViewHolder(root)
        setViewHolder(holder, viewType)
        return holder
    }

    override fun onViewAttachedToWindow(holder: VH) {
        super.onViewAttachedToWindow(holder)
    }

    /**
     * Should be called to set data into `Adapter`.
     */
    abstract fun setItems(items: List<T>)

    /**
     * Specifies way of creating instances of [VH].
     */
    protected abstract fun createViewHolder(itemView: View): VH

    /**
     * Specifies layout ID res, according to [viewType].
     */
    @LayoutRes
    protected abstract fun getItemViewLayout(viewType: Int): Int

    /**
     * Configurates specified [holder] and its view.
     */
    @CallSuper
    protected open fun setViewHolder(holder: VH, viewType: Int) {
        holder.itemView.setOnClickListener { performOnItemViewClick(holder) }
        holder.itemView.setOnLongClickListener { performOnItemViewLongClick(holder) }
    }

    /**
     * Sets [l] as current [clicksListener] object.
     * If [l] is `null`, it will be cleared.
     */
    fun setOnClicksListener(l: OnRecyclerItemClicksListener?) {
        this.clicksListener = l
    }

    /**
     * Would be called on [VH]'s view click.
     */
    private fun performOnItemViewClick(holder: VH) {
        val position = holder.bindingAdapterPosition
        onItemViewClick(holder)
        holder.onClick(holder.itemView)
        clicksListener?.onRecyclerItemClick(position)
    }

    /**
     * Would be called on [VH]'s view long click.
     */
    private fun performOnItemViewLongClick(holder: VH): Boolean {
        val position = holder.bindingAdapterPosition
        onItemViewLongClick(holder)
        val consumed = holder.onLongClick(holder.itemView)
        clicksListener?.onRecyclerItemLongClick(position)
        return consumed
    }

    open fun onItemViewClick(holder: VH) {
        // default empty implementation
    }

    open fun onItemViewLongClick(holder: VH) {
        // default empty implementation
    }

    /**
     * Inflates [getItemViewLayout] view.
     */
    protected open fun createItemView(parent: ViewGroup, viewType: Int): View {
        val layout = getItemViewLayout(viewType)
        return LayoutInflater.from(parent.context).inflate(layout, parent, false)
    }

    /**
     * @return `Unit`, if specified [index] is in [getItemCount] range,
     * `null` otherwise.
     */
    protected fun ensureIndexInItemsRange(index: Int): Unit? {
        if (index in 0 until itemCount) return Unit
        return null
    }
}