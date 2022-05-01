package com.feedback.android.app.common

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class RVAdapter<T>(
    private val onCreateVH: (ViewGroup, Int) -> BaseViewHolder<T>
) : RecyclerView.Adapter<BaseViewHolder<T>>() {

    private val items = mutableListOf<T>()
    var onBindAdditionalMethod: ((BaseViewHolder<T>, Int, T) -> Unit)? = null

    fun setItems(items: List<T>) {
        this.items.clear()
        notifyDataSetChanged()
        items.forEach { item ->
            if (!this.items.contains(item)) {
                this.items.add(item)
                notifyItemInserted(this.items.size - 1)
            }
        }
    }

    fun addItem(item: T) {
        if (!items.contains(item)) {
            items.add(item)
            notifyItemInserted(items.size - 1)
        }
    }

    fun updateItem(oldItem: T, newItem: T) {
        val pos = items.indexOf(oldItem)
        items[pos] = newItem
        notifyItemChanged(pos)
    }

    fun getItems(): List<T> = items

    fun removeItem(item: T) {
        val pos = items.indexOf(item)
        items.removeAt(pos)
        notifyItemRemoved(pos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        return onCreateVH(parent, viewType)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        val item = items[position]
        onBindAdditionalMethod?.invoke(holder, position, item)
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

}

open class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    open fun bind(item: T) {}
}
